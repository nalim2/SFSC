package zmq.processors;

import static protocol.pubsub.DataProtocol.TOPIC_FRAME;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageDistributor implements Consumer<byte[][]> {

  private static final Logger logger = LoggerFactory.getLogger(MessageDistributor.class);
  private final Set<TopicListener> topicListeners = ConcurrentHashMap.newKeySet();

  public void add(TopicListener topicListener) {
    topicListeners.add(topicListener);
  }

  public void remove(TopicListener topicListener) {
    topicListeners.remove(topicListener);
  }

  @Override
  public void accept(byte[][] message) {
    try {
      StringValue topic = TOPIC_FRAME.get(message, StringValue.parser());
      topicListeners.forEach(topicListener -> {
        if (topicListener.test(topic.getValue())) {
          topicListener.accept(message); //for performance reasons, all consumers share the original array //todo executor?
        }
      });
    } catch (InvalidProtocolBufferException e) {
      logger.warn("Received malformed topic", e);
    }
  }

  public interface TopicListener extends Predicate<String>, Consumer<byte[][]> {

  }

}
