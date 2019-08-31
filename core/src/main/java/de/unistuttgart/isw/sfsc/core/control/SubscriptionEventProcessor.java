package de.unistuttgart.isw.sfsc.core.control;

import static protocol.pubsub.SubProtocol.TYPE_AND_TOPIC_FRAME;

import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.protocol.control.SessionMessage;
import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.pubsub.SubProtocol;
import protocol.pubsub.SubProtocol.SubscriptionType;
import zmq.pubsubsocketpair.PubSubSocketPair.Publisher;

class SubscriptionEventProcessor implements Consumer<byte[][]> {

  private static final Logger logger = LoggerFactory.getLogger(SubscriptionEventProcessor.class);

  private final Publisher publisher;
  private final SessionManager sessionManager;

  SubscriptionEventProcessor(Publisher publisher, SessionManager sessionManager) {
    this.publisher = publisher;
    this.sessionManager = sessionManager;
  }

  @Override
  public void accept(byte[][] subscriptionMessage) {
    byte[] typeAndTopicFrame = TYPE_AND_TOPIC_FRAME.get(subscriptionMessage);
    SubscriptionType subscriptionType = SubProtocol.getSubscriptionType(typeAndTopicFrame);
    try {
      switch (subscriptionType) {
        case SUBSCRIPTION: {
          String topic = SubProtocol.getTopic(typeAndTopicFrame);
          WelcomeMessage welcome = sessionManager.onSubscription(topic);
          byte[] payload = SessionMessage.newBuilder().setWelcomeMessage(welcome).build().toByteArray();
          publisher.publish(topic, payload);
          break;
        }
        case UNSUBSCRIPTION: {
          String topic = SubProtocol.getTopic(typeAndTopicFrame);
          sessionManager.onUnsubscription(topic);
          break;
        }
        default: {
          logger.warn("Received unsupported subscription type {}", subscriptionType);
        }
      }
    } catch (
        InvalidProtocolBufferException e) {
      logger.warn("Received malformed topic", e);
    }
  }
}
