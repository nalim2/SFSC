package servicepatterns.pubsub;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol.PAYLOAD_FRAME;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager.TopicListener;
import de.unistuttgart.isw.sfsc.patterns.SfscError;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import servicepatterns.SfscMessage;
import servicepatterns.SfscMessageImpl;

class SubscriberTopicListener implements TopicListener {

  private final ByteString topic;
  private final Consumer<SfscMessage> consumer;
  private final Executor executor;

  SubscriberTopicListener(ByteString topic, Consumer<SfscMessage> consumer, Executor executor) {
    this.topic = topic;
    this.consumer = consumer;
    this.executor = executor;
  }

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
}
