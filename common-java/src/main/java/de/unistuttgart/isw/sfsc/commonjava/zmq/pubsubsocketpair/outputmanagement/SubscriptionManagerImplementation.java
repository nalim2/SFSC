package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol.buildTypeAndTopicFrame;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol.SubscriptionType;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Outbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionManagerImplementation implements SubscriptionManager {

  private static final Logger logger = LoggerFactory.getLogger(SubscriptionManagerImplementation.class);
  private final Outbox subscriptionOutbox;

  public SubscriptionManagerImplementation(Outbox subscriptionOutbox) {
    this.subscriptionOutbox = subscriptionOutbox;
  }

  @Override
  public Handle subscribe(ByteString topic) {
    logger.debug("Subscribing to topic {}", topic.toStringUtf8());
    sendSubscriptionMessage(SubscriptionType.SUBSCRIPTION, topic);
    return () -> {
      logger.debug("Unsubscribing from topic {}", topic.toStringUtf8());
      sendSubscriptionMessage(SubscriptionType.UNSUBSCRIPTION, topic);
    };
  }

  @Override
  public Handle subscribe(String topic) {
    return subscribe(ByteString.copyFromUtf8(topic));
  }

  @Override
  public Outbox outbox() {
    return subscriptionOutbox;
  }

  void sendSubscriptionMessage(SubscriptionType subscriptionType, ByteString topic) {
    byte[][] message = SubProtocol.newEmptyMessage();
    SubProtocol.TYPE_AND_TOPIC_FRAME.put(message, buildTypeAndTopicFrame(subscriptionType, topic));
    subscriptionOutbox.add(message);
  }
}
