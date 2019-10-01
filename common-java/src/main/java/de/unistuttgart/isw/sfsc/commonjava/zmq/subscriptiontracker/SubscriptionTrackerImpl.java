package de.unistuttgart.isw.sfsc.commonjava.zmq.subscriptiontracker;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.SubscriptionEventProcessor.SubscriptionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class SubscriptionTrackerImpl implements SubscriptionListener, SubscriptionTracker {

  private final Set<ByteString> subscriptions = new HashSet<>();
  private final Set<Consumer<ByteString>> subscriptionListeners = new HashSet<>();
  private final Set<Consumer<ByteString>> unsubscriptionListeners = new HashSet<>();

  public Set<ByteString> getSubscriptions() {
    return Collections.unmodifiableSet(subscriptions);
  }

  public void addSubscriptionListener(Consumer<ByteString> listener) {
    subscriptionListeners.add(listener);
  }

  public void removeSubscriptionListener(Consumer<ByteString> listener) {
    subscriptionListeners.remove(listener);
  }

  public void addUnsubscriptionListener(Consumer<ByteString> listener) {
    unsubscriptionListeners.add(listener);
  }

  public void removeUnsubscriptionListener(Consumer<ByteString> listener) {
    unsubscriptionListeners.remove(listener);
  }

  @Override
  public void onSubscription(ByteString topic) {
    subscriptions.add(topic);
    subscriptionListeners.forEach(consumer -> consumer.accept(topic));
  }

  @Override
  public void onUnsubscription(ByteString topic) {
    subscriptions.remove(topic);
    unsubscriptionListeners.forEach(consumer -> consumer.accept(topic));
  }
}
