package de.unistuttgart.isw.sfsc.commonjava.zmq.highlevelinbox;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol.TOPIC_FRAME;

import com.google.protobuf.ByteString;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TopicDistributor implements Consumer<byte[][]> {

  private static final Logger logger = LoggerFactory.getLogger(TopicDistributor.class);
  private final Set<TopicListener> topicListeners = ConcurrentHashMap.newKeySet();

  public void add(TopicListener topicListener) {
    topicListeners.add(topicListener);
  }

  public void remove(TopicListener topicListener) {
    topicListeners.remove(topicListener);
  }

  @Override
  public void accept(byte[][] message) {
    String topic = ByteString.copyFrom(TOPIC_FRAME.get(message)).toStringUtf8();
    topicListeners.forEach(topicListener -> {
      if (topicListener.test(topic)) {
        topicListener.processMessage(message); //for performance reasons, all consumers share the original array //todo executor?
      }
    });
  }

}
