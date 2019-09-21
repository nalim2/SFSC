package de.unistuttgart.isw.sfsc.commonjava.zmq.processors;

import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.SubscriptionEventProcessor.SubscriptionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SubscriptionTracker implements SubscriptionListener {

  private final Set<String> subscriptions = new HashSet<>();

  public Set<String> getSubscriptions() {
    return Collections.unmodifiableSet(subscriptions);
  }

  @Override
  public void onSubscription(String topic) {
    subscriptions.add(topic);
  }

  @Override
  public void onUnsubscription(String topic) {
    subscriptions.remove(topic);
  }
}
