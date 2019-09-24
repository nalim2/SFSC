package servicepatterns.pubsub;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.adapter.base.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager.InboxTopicManager;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import de.unistuttgart.isw.sfsc.patterns.Tags;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import servicepatterns.Service;
import servicepatterns.ServiceImpl;
import servicepatterns.SfscMessage;

public class PubSubFactory {

  private final ByteString adapterId;
  private final ByteString coreId;
  private final OutputPublisher publisher;
  private final InboxTopicManager inboxTopicManager;
  private final RegistryClient registryClient;

  public PubSubFactory(Adapter adapter) {
    publisher = adapter.publisher();
    inboxTopicManager = adapter.inboxTopicManager();
    registryClient = adapter.registryClient();
    adapterId = ByteString.copyFromUtf8(adapter.adapterId());
    coreId = ByteString.copyFromUtf8(adapter.coreId());
  }

  public Publisher publisher(ByteString topic, Map<String, ByteString> customTags) {
    Map<String, ByteString> publisherTags = complete(customTags);
    publisherTags.put(Tags.OUTPUT_TOPIC.name(), topic);
    return new PublisherImpl(publisherTags, publisher, () -> registryClient.removeService(publisherTags));
  }

  public Service subscriber(ByteString topic, Map<String, ByteString> customTags, Consumer<SfscMessage> consumer, Executor executor) {
    Map<String, ByteString> subscriberTags = complete(customTags);
    subscriberTags.put(Tags.INPUT_TOPIC.name(), topic);
    SubscriberTopicListener topicListener = new SubscriberTopicListener(topic, consumer, executor);
    inboxTopicManager.addTopicListener(topicListener);
    return new ServiceImpl(subscriberTags, () -> {
      inboxTopicManager.removeTopicListener(topicListener);
      registryClient.removeService(subscriberTags);
    });
  }

  Map<String, ByteString> complete(Map<String, ByteString> userTags) {
    Map<String, ByteString> tags = new HashMap<>(userTags);
    tags.put(Tags.SERVICE_ID.name(), ByteString.copyFromUtf8(UUID.randomUUID().toString()));
    tags.put(Tags.ADAPTER_ID.name(), adapterId);
    tags.put(Tags.CORE_ID.name(), coreId);
    return tags;
  }
}
