package de.unistuttgart.isw.sfsc.commonjava.zmq.processors;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol.TYPE_AND_TOPIC_FRAME;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol.SubscriptionType;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionEventProcessor implements Consumer<byte[][]> {

  private static final Logger logger = LoggerFactory.getLogger(SubscriptionEventProcessor.class);

  private final SubscriptionListener subscriptionListener;

  public SubscriptionEventProcessor(SubscriptionListener subscriptionListener) {
    this.subscriptionListener = subscriptionListener;
  }

  @Override
  public void accept(byte[][] subscriptionMessage) {
    byte[] typeAndTopicFrame = TYPE_AND_TOPIC_FRAME.get(subscriptionMessage);
    SubscriptionType subscriptionType = SubProtocol.getSubscriptionType(typeAndTopicFrame);
      switch (subscriptionType) {
        case SUBSCRIPTION: {
          ByteString topic = SubProtocol.getTopicMessage(typeAndTopicFrame);
          subscriptionListener.onSubscription(topic);
          break;
        }
        case UNSUBSCRIPTION: {
          ByteString topic = SubProtocol.getTopicMessage(typeAndTopicFrame);
          subscriptionListener.onUnsubscription(topic);
          break;
        }
        default: {
          logger.warn("Received unsupported subscription type {}", subscriptionType);
          break;
        }
      }
  }

  public interface SubscriptionListener {

    void onSubscription(ByteString topic);

    void onUnsubscription(ByteString topic);
  }
}
