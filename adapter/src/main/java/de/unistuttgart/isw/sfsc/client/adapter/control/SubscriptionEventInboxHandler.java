package de.unistuttgart.isw.sfsc.client.adapter.control;

import java.util.HashSet;
import java.util.Set;
import zmq.processors.SubscriptionEventProcessor.SubscriptionListener;

class SubscriptionEventInboxHandler implements SubscriptionListener {

  private static final Set<String> TOPICS = Set.of("registry", "session");

  private final Set<String> missing = new HashSet<>(TOPICS);
  private final Runnable ready;

  SubscriptionEventInboxHandler(Runnable ready) {
    this.ready = ready;
  }

  @Override
  public void onSubscription(String topic) {
    missing.remove(topic);
    if (missing.isEmpty()) {
      ready.run();
    }
  }

  @Override
  public void onUnsubscription(String topic) {
  }


}
