package de.unistuttgart.isw.sfsc.commonjava.zmq.processors;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol.TOPIC_FRAME;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
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
          topicListener.processMessage(message); //for performance reasons, all consumers share the original array //todo executor?
        }
      });
    } catch (InvalidProtocolBufferException e) {
      logger.warn("Received malformed topic", e);
    }
  }

  public interface TopicListener {

    String getTopic();

    boolean test(String topic);

    void processMessage(byte[][] message);
  }
}
