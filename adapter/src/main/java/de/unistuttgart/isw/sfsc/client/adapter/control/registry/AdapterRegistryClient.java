package de.unistuttgart.isw.sfsc.client.adapter.control.registry;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol.PAYLOAD_FRAME;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.commonjava.registry.TimeoutRegistry;
import de.unistuttgart.isw.sfsc.commonjava.util.ConsumerFuture;
import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.MessageDistributor.TopicListener;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.Publisher;
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
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdapterRegistryClient implements RegistryClient, TopicListener, AutoCloseable {

  public static final String TOPIC = "de/unistuttgart/isw/sfsc/commonjava/registry";
  private static final int DEFAULT_TIMEOUT_MS = 500; //todo

  private static final Logger logger = LoggerFactory.getLogger(AdapterRegistryClient.class);

  private final Supplier<Integer> idSupplier = new AtomicInteger()::getAndIncrement;
  private final Consumer<Exception> exceptionConsumer = exception -> logger.warn("de.unistuttgart.isw.sfsc.commonjava.registry created exception", exception);
  private final TimeoutRegistry<Integer, Consumer<? super Message>> timeoutRegistry = new TimeoutRegistry<>();
  private final Publisher publisher;
  private final String topic;

  AdapterRegistryClient(Publisher publisher, String name) {
    this.publisher = publisher;
    topic = TOPIC + "///" + name; //todo ///
  }

  public static AdapterRegistryClient create(Publisher publisher, String name) {
    return new AdapterRegistryClient(publisher, name);
  }

  @Override
  public Future<ServiceHandle> addService(ServiceDeclaration serviceDeclaration) {
    UUID serviceTopic = UUID.randomUUID();
    ServiceHandle serviceHandle = new ServiceHandle(serviceTopic.toString(), serviceDeclaration.getName());
    ConsumerFuture<Message, ServiceHandle> consumerFuture = new ConsumerFuture<>(message -> serviceHandle);
    int id = idSupplier.get();
    RegistryMessage message = RegistryMessage.newBuilder().setMessageId(id).setCreateRequest(CreateRequest.newBuilder()
        .setService(ServiceDescriptor.newBuilder().setName(serviceDeclaration.getName()).setTopic(serviceTopic.toString()).build()).build()).build();
    timeoutRegistry.put(id, consumerFuture, DEFAULT_TIMEOUT_MS, exceptionConsumer);
    publisher.publish(topic, message);
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
    RegistryMessage message = RegistryMessage.newBuilder().setMessageId(id).setReadRequest(ReadRequest.newBuilder().build()).build();
    timeoutRegistry.put(id, consumerFuture, DEFAULT_TIMEOUT_MS, exceptionConsumer);
    publisher.publish(topic, message);
    return consumerFuture;
  }

  @Override
  public Future<Void> removeService(ServiceHandle serviceHandle) {
    ConsumerFuture<Message, Void> consumerFuture = new ConsumerFuture<>(ignored -> null);
    int id = idSupplier.get();
    RegistryMessage message = RegistryMessage.newBuilder().setMessageId(id).setDeleteRequest(DeleteRequest.newBuilder()
        .setService(ServiceDescriptor.newBuilder().setName(serviceHandle.getName()).setTopic(serviceHandle.getTopic()).build()).build()).build();
    timeoutRegistry.put(id, consumerFuture, DEFAULT_TIMEOUT_MS, exceptionConsumer);
    publisher.publish(topic, message);
    return consumerFuture;
  }

  @Override
  public String getTopic() {
    return topic;
  }

  @Override
  public boolean test(String topic) {
    return topic.equals(this.topic);
  }

  @Override
  public void processMessage(byte[][] message) {
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
            logger.warn("received de.unistuttgart.isw.sfsc.commonjava.registry message with currently unsupported type {}", reply.getPayloadCase());
            break;
          }

        }
      }
    } catch (InvalidProtocolBufferException e) {
      logger.warn("received malformed message", e);
    }
  }

  @Override
  public void close() {
    timeoutRegistry.close();
  }

}
