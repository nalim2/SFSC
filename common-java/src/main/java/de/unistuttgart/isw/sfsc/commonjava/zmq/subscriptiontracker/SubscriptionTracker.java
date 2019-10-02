package de.unistuttgart.isw.sfsc.commonjava.zmq.subscriptiontracker;

import com.google.protobuf.ByteString;
import java.util.Set;
import java.util.function.Consumer;

public interface SubscriptionTracker {

  Set<ByteString> getSubscriptions();

  void addSubscriptionListener(Consumer<ByteString> listener);

  void removeSubscriptionListener(Consumer<ByteString> listener);

  void addUnsubscriptionListener(Consumer<ByteString> listener);

  void removeUnsubscriptionListener(Consumer<ByteString> listener);
}
