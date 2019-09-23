package de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol.PAYLOAD_FRAME;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.SfscMessage;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.SfscMessageImpl;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.tags.TagCompleter;
import de.unistuttgart.isw.sfsc.client.adapter.raw.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager.InboxManager;
import de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager.TopicListener;
import de.unistuttgart.isw.sfsc.patterns.SfscError;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor.Tags;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class SubscriberFactory {

  private final TagCompleter tagCompleter;
  private final RegistryClient registryClient;
  private final InboxManager inboxManager;

  public SubscriberFactory(TagCompleter tagCompleter, RegistryClient registryClient, InboxManager inboxManager) {
    this.tagCompleter = tagCompleter;
    this.registryClient = registryClient;
    this.inboxManager = inboxManager;
  }

  public Subscriber subscriber(Map<String, ByteString> tags, Consumer<SfscMessage> consumer, Executor executor) {
    Map<String, ByteString> subscriberTags = tagCompleter.completeSubscriber(tags);

    TopicListener topicListener = new TopicListener() {
      ByteString topic = subscriberTags.get(Tags.TOPIC.name());

      @Override
      public Set<ByteString> getTopics() {
        return Set.of(topic);
      }

      @Override
      public boolean test(ByteString topic) {
        return this.topic.equals(topic);
      }

      @Override
      public void processMessage(byte[][] message) {
        executor.execute(() -> consumer.accept(new SfscMessageImpl(SfscError.NO_ERROR, ByteString.copyFrom(PAYLOAD_FRAME.get(message)))));
      }
    };

    inboxManager.addTopic(topicListener);
    return new SubscriberImpl(subscriberTags, () -> {
      inboxManager.removeTopic(topicListener);
      registryClient.removeService(subscriberTags);
    });
  }
}
