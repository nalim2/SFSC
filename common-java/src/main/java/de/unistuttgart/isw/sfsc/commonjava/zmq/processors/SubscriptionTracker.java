package de.unistuttgart.isw.sfsc.commonjava.zmq.processors;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.SubscriptionEventProcessor.SubscriptionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SubscriptionTracker implements SubscriptionListener {

  private final Set<ByteString> subscriptions = new HashSet<>();

  public Set<ByteString> getSubscriptions() {
    return Collections.unmodifiableSet(subscriptions);
  }

  @Override
  public void onSubscription(ByteString topic) {
    subscriptions.add(topic);
  }

  @Override
  public void onUnsubscription(ByteString topic) {
    subscriptions.remove(topic);
  }
}
