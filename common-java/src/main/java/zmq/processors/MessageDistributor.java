package zmq.processors;

import static protocol.pubsub.DataProtocol.TOPIC_FRAME;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MessageDistributor implements Consumer<byte[][]> {

  private final Set<TopicListener> topicListeners = ConcurrentHashMap.newKeySet();

  public void add(TopicListener topicListener) {
    topicListeners.add(topicListener);
  }

  public void remove(TopicListener topicListener) {
    topicListeners.remove(topicListener);
  }

  @Override
  public void accept(byte[][] message) {
    byte[] topic = TOPIC_FRAME.get(message);
    topicListeners.forEach(topicListener -> {
      if (topicListener.test(topic)) {
        topicListener.accept(message); //for performance reasons, all consumers share the original array
      }
    });
  }

  public interface TopicListener extends Predicate<byte[]>, Consumer<byte[][]> {

  }

}
