package zmq.processors;

import static protocol.pubsub.DataProtocol.TOPIC_FRAME;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class MessageDistributor implements Consumer<byte[][]> {

  private final Set<TopicListener> topicListeners = ConcurrentHashMap.newKeySet();

  public void add(TopicListener topicListener){
    topicListeners.add(topicListener);
  }

  public void remove(TopicListener topicListener){
    topicListeners.remove(topicListener);
  }

  @Override
  public void accept(byte[][] message) {
    String topic = new String(TOPIC_FRAME.get(message));
    topicListeners.forEach(topicListener -> {
      if (topicListener.getTopicPattern().matcher(topic).matches()) {
        topicListener.accept(message);
      }
    });
  }

  public interface TopicListener extends Consumer<byte[][]> {

    Pattern getTopicPattern();
  }
}
