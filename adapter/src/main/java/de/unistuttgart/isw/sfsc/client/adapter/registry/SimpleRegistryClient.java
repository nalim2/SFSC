package de.unistuttgart.isw.sfsc.client.adapter.registry;

import static protocol.pubsub.DataProtocol.PAYLOAD_FRAME;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import consumerfuture.ConsumerFuture;
import de.unistuttgart.isw.sfsc.protocol.registry.CreateRequest;
import de.unistuttgart.isw.sfsc.protocol.registry.DeleteRequest;
import de.unistuttgart.isw.sfsc.protocol.registry.ReadRequest;
import de.unistuttgart.isw.sfsc.protocol.registry.ReadResponse;
import de.unistuttgart.isw.sfsc.protocol.registry.RegistryMessage;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import registry.TimeoutRegistry;
import zmq.processors.MessageDistributor.TopicListener;
import zmq.pubsubsocketpair.PubSubSocketPair.Publisher;

public class SimpleRegistryClient implements RegistryClient, TopicListener, AutoCloseable {

  private static final String REGISTRY_TOPIC = "registry";
  private static final int DEFAULT_TIMEOUT_MS = 500; //todo

  private static final Logger logger = LoggerFactory.getLogger(SimpleRegistryClient.class);

  private final Supplier<Integer> idSupplier = new AtomicInteger()::getAndIncrement;
  private final Consumer<Exception> exceptionConsumer = exception -> logger.warn("registry created exception", exception);
  private final TimeoutRegistry<Integer, Consumer<? super Message>> timeoutRegistry = new TimeoutRegistry<>();
  private final Publisher publisher;
  private final Pattern pattern;
  private final String topic;

  SimpleRegistryClient(Publisher publisher, UUID uuid) {
    topic = REGISTRY_TOPIC + "///" + uuid; //todo ///
    pattern = Pattern.compile("\\A" + topic + "\\z");
    this.publisher = publisher;
  }

  public static SimpleRegistryClient create(Publisher publisher, UUID uuid) {
    return new SimpleRegistryClient(publisher, uuid);
  }

  @Override
  public Future<ServiceHandle> addService(ServiceDeclaration serviceDeclaration) {
    UUID serviceTopic = UUID.randomUUID();
    ServiceHandle serviceHandle = new ServiceHandle(serviceTopic.toString(), serviceDeclaration.getName());
    ConsumerFuture<Message, ServiceHandle> consumerFuture = new ConsumerFuture<>(message -> serviceHandle);
    int id = idSupplier.get();
    byte[] message = RegistryMessage.newBuilder().setMessageId(id).setCreateRequest(CreateRequest.newBuilder()
        .setService(ServiceDescriptor.newBuilder().setName(serviceDeclaration.getName()).setTopic(serviceTopic.toString()).build()).build()).build()
        .toByteArray();
    timeoutRegistry.put(id, consumerFuture, DEFAULT_TIMEOUT_MS, exceptionConsumer);
    publisher.publish(topic.getBytes(), message);
    return consumerFuture;
  }

  @Override
  public Future<Set<ServiceHandle>> getServices() {
    ConsumerFuture<Message, Set<ServiceHandle>> consumerFuture = new ConsumerFuture<>(message ->
        ((ReadResponse) message)
            .getServicesList()
            .stream()
            .map(serviceDescriptor -> new ServiceHandle(serviceDescriptor.getTopic(), serviceDescriptor.getName()))
            .collect(Collectors.toSet()));
    int id = idSupplier.get();
    byte[] message = RegistryMessage.newBuilder().setMessageId(id).setReadRequest(ReadRequest.newBuilder().build()).build().toByteArray();
    timeoutRegistry.put(id, consumerFuture, DEFAULT_TIMEOUT_MS, exceptionConsumer);
    publisher.publish(topic.getBytes(), message);
    return consumerFuture;
  }

  @Override
  public Future<Void> removeService(ServiceHandle serviceHandle) {
    ConsumerFuture<Message, Void> consumerFuture = new ConsumerFuture<>(ignored -> null);
    int id = idSupplier.get();
    byte[] message = RegistryMessage.newBuilder().setMessageId(id).setDeleteRequest(DeleteRequest.newBuilder()
        .setService(ServiceDescriptor.newBuilder().setName(serviceHandle.getName()).setTopic(serviceHandle.getTopic()).build()).build()).build()
        .toByteArray();
    timeoutRegistry.put(id, consumerFuture, DEFAULT_TIMEOUT_MS, exceptionConsumer);
    publisher.publish(topic.getBytes(), message);
    return consumerFuture;
  }

  @Override
  public boolean test(byte[] bytes) {
    return pattern.matcher(new String(bytes)).matches();
  }

  @Override
  public void accept(byte[][] message) {
    try {
      RegistryMessage reply = PAYLOAD_FRAME.get(message, RegistryMessage.parser());
      Consumer<? super Message> consumer = timeoutRegistry.remove(reply.getMessageId());
      if (consumer != null) {
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
    } catch (InvalidProtocolBufferException e) {
      logger.warn("received malformed message", e);
    }
  }

  public String getTopic() {
    return topic;
  }

  @Override
  public void close() {
    timeoutRegistry.close();
  }

}
