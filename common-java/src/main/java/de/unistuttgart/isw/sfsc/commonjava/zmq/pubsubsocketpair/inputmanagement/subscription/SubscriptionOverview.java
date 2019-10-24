package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription;

import com.google.protobuf.ByteString;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SubscriptionOverview {

  private static final Logger logger = LoggerFactory.getLogger(SubscriptionOverview.class);
  private final Set<ByteString> subscriptions = new HashSet<>();

  Set<ByteString> getSubscriptions() {
    return Collections.unmodifiableSet(subscriptions);
  }

  void onSubscription(ByteString topic) {
    subscriptions.add(topic);
    logger.debug("Received subscription on topic {}", topic.toStringUtf8());
  }

  void onUnsubscription(ByteString topic) {
    subscriptions.remove(topic);
    logger.debug("Received unsubscription on topic {}", topic.toStringUtf8());
  }
}
