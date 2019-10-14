package servicepatterns.topiclistener;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol.PAYLOAD_FRAME;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager.TopicListener;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

class SingleTopicListener implements TopicListener {

  private final ByteString topic;
  private final Consumer<ByteString> consumer;
  private final Executor executor;

  SingleTopicListener(ByteString topic, Consumer<ByteString> consumer, Executor executor) {
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
    executor.execute(() -> consumer.accept(ByteString.copyFrom(PAYLOAD_FRAME.get(message))));
  }
}
