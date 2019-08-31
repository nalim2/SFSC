package de.unistuttgart.isw.sfsc.client.adapter.control;

import static protocol.pubsub.SubProtocol.TYPE_AND_TOPIC_FRAME;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.pubsub.SubProtocol;
import protocol.pubsub.SubProtocol.SubscriptionType;

class SubscriptionEventInboxHandler implements Consumer<byte[][]> {

  private static final Set<String> TOPICS = Set.of("registry", "session");
  private static final Logger logger = LoggerFactory.getLogger(SubscriptionEventInboxHandler.class);

  private final Set<String> missing = new HashSet<>(TOPICS);
  private final Runnable ready;

  SubscriptionEventInboxHandler(Runnable ready) {
    this.ready = ready;
  }

  @Override
  public void accept(byte[][] subscriptionMessage) {
    byte[] typeAndTopicFrame = TYPE_AND_TOPIC_FRAME.get(subscriptionMessage);
    SubscriptionType subscriptionType = SubProtocol.getSubscriptionType(typeAndTopicFrame);
    try {
      switch (subscriptionType) {
        case SUBSCRIPTION: {
          missing.remove(SubProtocol.getTopic(typeAndTopicFrame));
          if (missing.isEmpty()) {
            ready.run();
          }
          break;
        }
        default: {
          logger.warn("Received unsupported subscription event with type {}", subscriptionType);
          break;
        }
      }
    } catch (
        InvalidProtocolBufferException e) {
      logger.warn("Received malformed topic", e);
    }
  }

}
