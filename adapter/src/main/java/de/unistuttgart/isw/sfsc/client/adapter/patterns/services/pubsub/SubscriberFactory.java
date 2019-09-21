package de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol.PAYLOAD_FRAME;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.SfscMessage;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.SfscMessageImpl;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.tags.TagCompleter;
import de.unistuttgart.isw.sfsc.client.adapter.raw.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.commonjava.zmq.comfortinbox.ComfortInbox;
import de.unistuttgart.isw.sfsc.commonjava.zmq.comfortinbox.TopicListener;
import de.unistuttgart.isw.sfsc.patterns.SfscError;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor.Tags;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class SubscriberFactory {

  private final TagCompleter tagCompleter;
  private final RegistryClient registryClient;
  private final ComfortInbox comfortInbox;

  public SubscriberFactory(TagCompleter tagCompleter, RegistryClient registryClient, ComfortInbox comfortInbox) {
    this.tagCompleter = tagCompleter;
    this.registryClient = registryClient;
    this.comfortInbox = comfortInbox;
  }

  public Subscriber subscriber(Map<String, ByteString> tags, Consumer<SfscMessage> consumer, Executor executor) {
      Map<String, ByteString> subscriberTags = tagCompleter.completeSubscriber(tags);

      TopicListener topicListener = new TopicListener() {
        ByteString topic = subscriberTags.get(Tags.TOPIC.name());

        @Override
        public ByteString getTopic() {
          return topic;
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

      comfortInbox.addTopic(topicListener);
      return new SubscriberImpl(subscriberTags, () -> {
        comfortInbox.removeTopic(topicListener);
        registryClient.removeService(subscriberTags);
      });
  }
}
