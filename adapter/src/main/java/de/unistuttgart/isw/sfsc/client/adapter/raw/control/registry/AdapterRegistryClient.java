package de.unistuttgart.isw.sfsc.client.adapter.raw.control.registry;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol.PAYLOAD_FRAME;
import static de.unistuttgart.isw.sfsc.commonjava.registry.TimeoutRegistry.DEFAULT_TIMEOUT_MS;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.commonjava.registry.TimeoutRegistry;
import de.unistuttgart.isw.sfsc.commonjava.util.ConsumerFuture;
import de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager.TopicListener;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import de.unistuttgart.isw.sfsc.protocol.registry.CreateRequest;
import de.unistuttgart.isw.sfsc.protocol.registry.CreateResponse;
import de.unistuttgart.isw.sfsc.protocol.registry.DeleteRequest;
import de.unistuttgart.isw.sfsc.protocol.registry.ReadRequest;
import de.unistuttgart.isw.sfsc.protocol.registry.ReadResponse;
import de.unistuttgart.isw.sfsc.protocol.registry.RegistryMessage;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdapterRegistryClient implements RegistryClient, TopicListener, AutoCloseable {

  public static final String TOPIC = "registry";

  private static final Logger logger = LoggerFactory.getLogger(AdapterRegistryClient.class);

  private final Supplier<Integer> idSupplier = new AtomicInteger()::getAndIncrement;
  private final Runnable timeoutRunnable = () -> logger.warn("registry timeout");
  private final TimeoutRegistry<Integer, Consumer<? super Message>> timeoutRegistry = new TimeoutRegistry<>();
  private final OutputPublisher publisher;
  private final ByteString topic;

  AdapterRegistryClient(OutputPublisher publisher, String name) {
    this.publisher = publisher;
    topic = ByteString.copyFromUtf8(TOPIC + "://" + name);
  }

  public static AdapterRegistryClient create(OutputPublisher publisher, String name) {
    return new AdapterRegistryClient(publisher, name);
  }

  @Override
  public Future<Map<String, ByteString>> addService(Map<String, ByteString> service) {
    ConsumerFuture<Message, Map<String, ByteString>> consumerFuture = new ConsumerFuture<>(message ->
        ((CreateResponse) message)
            .getService()
            .getTagsMap()
    );

    int id = idSupplier.get();
    RegistryMessage message = RegistryMessage
        .newBuilder()
        .setMessageId(id)
        .setCreateRequest(CreateRequest
            .newBuilder()
            .setService(ServiceDescriptor.newBuilder().putAllTags(service).build())
            .build())
        .build();
    timeoutRegistry.put(id, consumerFuture, DEFAULT_TIMEOUT_MS, timeoutRunnable);

    publisher.publish(topic, message);
    return consumerFuture;
  }

  @Override
  public Future<Set<Map<String, ByteString>>> getServices() {
    ConsumerFuture<Message, Set<Map<String, ByteString>>> consumerFuture = new ConsumerFuture<>(message ->
        ((ReadResponse) message)
            .getServicesList()
            .stream()
            .map(ServiceDescriptor::getTagsMap)
            .collect(Collectors.toSet())
    );

    int id = idSupplier.get();
    RegistryMessage message = RegistryMessage
        .newBuilder()
        .setMessageId(id)
        .setReadRequest(ReadRequest.getDefaultInstance())
        .build();
    timeoutRegistry.put(id, consumerFuture, DEFAULT_TIMEOUT_MS, timeoutRunnable);

    publisher.publish(topic, message);
    return consumerFuture;
  }

  @Override
  public Future<Void> removeService(Map<String, ByteString> service) {
    ConsumerFuture<Message, Void> consumerFuture = new ConsumerFuture<>(ignored -> null);

    int id = idSupplier.get();
    RegistryMessage message = RegistryMessage.newBuilder().setMessageId(id)
        .setDeleteRequest(DeleteRequest
            .newBuilder()
            .setService(ServiceDescriptor.newBuilder().putAllTags(service).build())
            .build())
        .build();
    timeoutRegistry.put(id, consumerFuture, DEFAULT_TIMEOUT_MS, timeoutRunnable);

    publisher.publish(topic, message);
    return consumerFuture;
  }

  @Override
  public Set<ByteString> getTopics() {
    return Set.of(topic);
  }

  @Override
  public boolean test(ByteString topic) {
    return topic.equals(this.topic);
  }

  @Override
  public void processMessage(byte[][] message) {
    try {
      RegistryMessage reply = PAYLOAD_FRAME.get(message, RegistryMessage.parser());
      timeoutRegistry.remove(reply.getMessageId()).ifPresent(consumer -> {
            switch (reply.getPayloadCase()) {
              case CREATE_RESPONSE: {
                consumer.accept(reply.getCreateResponse());
                break;
              }
              case READ_RESPONSE: {
                consumer.accept(reply.getReadResponse());
                break;
              }
              case DELETE_RESPONSE: {
                consumer.accept(reply.getDeleteResponse());
                break;
              }
              default: {
                logger.warn("received registry message with currently unsupported type {}", reply.getPayloadCase());
                break;
              }
            }
          }
      );
    } catch (InvalidProtocolBufferException e) {
      logger.warn("received malformed message", e);
    }
  }

  @Override
  public void close() {
    timeoutRegistry.close();
  }

}
