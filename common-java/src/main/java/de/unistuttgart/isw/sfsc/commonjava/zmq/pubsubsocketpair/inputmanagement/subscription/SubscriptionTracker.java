package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface SubscriptionTracker {

  Handle addSubscriptionListener(Consumer<ByteString> onSubscription);

  Handle addUnsubscriptionListener(Consumer<ByteString> onUnsubscription);

  <V> Future<V> addSubscriptionListener(ByteString topic, Callable<V> callable);

  <V> Future<V> addSubscriptionListener(ByteString topic, Runnable runnable, V result);

  <V> Future<V> addUnsubscriptionListener(ByteString topic, Callable<V> callable);

  <V> Future<V> addUnsubscriptionListener(ByteString topic, Runnable runnable, V result);

  Set<ByteString> getSubscriptions();
}
