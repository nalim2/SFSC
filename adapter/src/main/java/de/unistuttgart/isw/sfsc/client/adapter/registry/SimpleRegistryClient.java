package de.unistuttgart.isw.sfsc.client.adapter.registry;

import static protocol.pubsub.DataProtocol.PAYLOAD_FRAME;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.protocol.registry.CreateRequest;
import de.unistuttgart.isw.sfsc.protocol.registry.CreateResponse;
import de.unistuttgart.isw.sfsc.protocol.registry.DeleteRequest;
import de.unistuttgart.isw.sfsc.protocol.registry.DeleteResponse;
import de.unistuttgart.isw.sfsc.protocol.registry.ReadRequest;
import de.unistuttgart.isw.sfsc.protocol.registry.ReadResponse;
import de.unistuttgart.isw.sfsc.protocol.registry.RegistryMessage;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
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

  private final Supplier<Integer> idSupplier = new AtomicInteger()::getAndIncrement;

  private static final Logger logger = LoggerFactory.getLogger(SimpleRegistryClient.class);

  private final Pattern pattern;
  private final Publisher publisher;
  private final TimeoutRegistry<Integer, Consumer<Message>> timeoutRegistry = new TimeoutRegistry<>();
  private final Consumer<Exception> exceptionConsumer = exception -> logger.warn("registry created exception", exception);
  private final int timeoutMs = 500;
  private final String fullTopic;

  SimpleRegistryClient(Publisher publisher, UUID uuid) {
    fullTopic = REGISTRY_TOPIC + "///" + uuid;
    this.pattern = Pattern.compile("\\A" + fullTopic + "\\z");
    this.publisher = publisher;
  }

  public static SimpleRegistryClient create(Publisher publisher, UUID uuid) {
    return new SimpleRegistryClient(publisher, uuid);
  }

  @Override
  public ServiceHandle addService(ServiceDeclaration serviceDeclaration) throws ExecutionException, InterruptedException {
    UUID topic = UUID.randomUUID();
    ServiceHandle serviceHandle = new ServiceHandle(topic.toString(), serviceDeclaration.getName());
    MessageConsumer<CreateResponse> responseConsumer = new MessageConsumer<>(CreateResponse.class);
    int id = idSupplier.get();
    byte[] message = RegistryMessage.newBuilder().setMessageId(id).setCreateRequest(CreateRequest.newBuilder()
        .setService(ServiceDescriptor.newBuilder().setName(serviceDeclaration.getName()).setTopic(topic.toString()).build()).build()).build().toByteArray();
    timeoutRegistry.put(id, responseConsumer, timeoutMs, exceptionConsumer);
    publisher.publish(fullTopic.getBytes(), message);
    responseConsumer.future.get();
    return serviceHandle;
  }

  @Override
  public Set<ServiceHandle> getServices() throws ExecutionException, InterruptedException {
    MessageConsumer<ReadResponse> responseConsumer = new MessageConsumer<>(ReadResponse.class);
    int id = idSupplier.get();
    byte[] message = RegistryMessage.newBuilder().setMessageId(id).setReadRequest(ReadRequest.newBuilder().build()).build().toByteArray();
    timeoutRegistry.put(id, responseConsumer, timeoutMs, exceptionConsumer);
    publisher.publish(fullTopic.getBytes(), message);
    return responseConsumer.future.get().getServicesList()
        .stream()
        .map(serviceDescriptor -> new ServiceHandle(serviceDescriptor.getTopic(), serviceDescriptor.getName()))
        .collect(Collectors.toSet());
  }

  @Override
  public void removeService(ServiceHandle serviceHandle) throws ExecutionException, InterruptedException {
    MessageConsumer<DeleteResponse> responseConsumer = new MessageConsumer<>(DeleteResponse.class);
    int id = idSupplier.get();
    byte[] message = RegistryMessage.newBuilder().setMessageId(id).setDeleteRequest(DeleteRequest.newBuilder()
        .setService(ServiceDescriptor.newBuilder().setName(serviceHandle.getName()).setTopic(serviceHandle.getTopic()).build()).build()).build().toByteArray();
    timeoutRegistry.put(id, responseConsumer, timeoutMs, exceptionConsumer);
    publisher.publish(fullTopic.getBytes(), message);
    responseConsumer.future.get();
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

  @Override
  public void close() {
    timeoutRegistry.close();
  }

  static class MessageConsumer<T> implements Consumer<Message> {

    private volatile T message;
    private final FutureTask<T> future = new FutureTask<>(() -> message);
    private final Class<T> clazz;

    MessageConsumer(Class<T> clazz) {
      this.clazz = clazz;
    }

    @Override
    public void accept(Message message) {
      this.message = clazz.cast(message);
      future.run();
    }

    Future<T> future() {
      return future;
    }
  }

}
