package de.unistuttgart.isw.sfsc.client.adapter.patterns.services.reqrep;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.client.adapter.raw.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.commonjava.registry.TimeoutRegistry;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.Publisher;
import de.unistuttgart.isw.sfsc.patterns.reqrep.RequestReplyMessage;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor.Tags;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.SfscMessage;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.SfscMessageImpl;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub.SubscriberFactory;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.tags.TagCompleter;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientFactory {

  private final TagCompleter tagCompleter;
  private final PubSubConnection.Publisher publisher;
  private final RegistryClient registryClient;
  private final TimeoutRegistry<Integer, Consumer<SfscMessage>> timeoutRegistry;
  private final Supplier<Integer> idGenerator = new AtomicInteger()::getAndIncrement;
  private final SubscriberFactory subscriberFactory;

  public ClientFactory(TagCompleter tagCompleter, Publisher publisher, RegistryClient registryClient,
      TimeoutRegistry<Integer, Consumer<SfscMessage>> timeoutRegistry, SubscriberFactory subscriberFactory) {
    this.tagCompleter = tagCompleter;
    this.publisher = publisher;
    this.registryClient = registryClient;
    this.timeoutRegistry = timeoutRegistry;
    this.subscriberFactory = subscriberFactory;
  }

  public Client client(Map<String, ByteString> tags, Executor executor) {
    Map<String, ByteString> clientTags = tagCompleter.completeClient(tags);
    Map<String, ByteString> subscriberTags = tagCompleter.completeSubscriber(Collections.emptyMap());

    subscriberFactory.subscriber(subscriberTags, new ClientConsumer(timeoutRegistry), executor);
    return new ClientImpl(clientTags, idGenerator, timeoutRegistry, subscriberTags.get(Tags.TOPIC.name()), publisher,
        () -> registryClient.removeService(clientTags));
  }

  static class ClientConsumer implements Consumer<SfscMessage> {

    private static final Logger logger = LoggerFactory.getLogger(ClientConsumer.class);
    private final TimeoutRegistry<Integer, Consumer<SfscMessage>> timeoutRegistry;

    ClientConsumer(TimeoutRegistry<Integer, Consumer<SfscMessage>> timeoutRegistry) {
      this.timeoutRegistry = timeoutRegistry;
    }

    @Override
    public void accept(SfscMessage sfscMessage) {
      try {
        RequestReplyMessage response = RequestReplyMessage.parseFrom(sfscMessage.getPayload());
        timeoutRegistry.remove(response.getMessageId()).accept(new SfscMessageImpl(sfscMessage.getError(), response.getPayload()));
      } catch (InvalidProtocolBufferException e) {
        logger.warn("Received malformed message", e);
      }
    }
  }

}
