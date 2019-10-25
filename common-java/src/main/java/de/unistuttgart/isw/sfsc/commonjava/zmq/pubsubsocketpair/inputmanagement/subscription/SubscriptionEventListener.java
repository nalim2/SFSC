package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.OneShotListener;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Predicate;

class SubscriptionEventListener {

  static <V> Future<V> addSubscriptionListener(SubscriptionTracker subscriptionTracker, Predicate<ByteString> predicate, Callable<V> callable) {
    OneShotListener<ByteString, V> oneShotListener = new OneShotListener<>(predicate, callable);
    Handle handle = subscriptionTracker.addSubscriptionListener(oneShotListener);
    Future<V> future = oneShotListener.initialize(handle);
    subscriptionTracker.getSubscriptions().forEach(oneShotListener);
    return future;
  }

  static <V> Future<V> addSubscriptionListener(SubscriptionTracker subscriptionTracker, Predicate<ByteString> predicate, Runnable runnable, V result) {
    OneShotListener<ByteString, V> oneShotListener = new OneShotListener<>(predicate, runnable, result);
    Handle handle = subscriptionTracker.addSubscriptionListener(oneShotListener);
    Future<V> future = oneShotListener.initialize(handle);
    subscriptionTracker.getSubscriptions().forEach(oneShotListener);
    return future;
  }

  static <V> Future<V> addUnsubscriptionListener(SubscriptionTracker subscriptionTracker, Predicate<ByteString> predicate, Callable<V> callable) {
    OneShotListener<ByteString, V> oneShotListener = new OneShotListener<>(predicate, callable);
    Handle handle = subscriptionTracker.addUnsubscriptionListener(oneShotListener);
    return oneShotListener.initialize(handle);
  }

  static <V> Future<V> addUnsubscriptionListener(SubscriptionTracker subscriptionTracker, Predicate<ByteString> predicate, Runnable runnable, V result) {
    OneShotListener<ByteString, V> oneShotListener = new OneShotListener<>(predicate, runnable, result);
    Handle handle = subscriptionTracker.addUnsubscriptionListener(oneShotListener);
    return oneShotListener.initialize(handle);
  }

}
