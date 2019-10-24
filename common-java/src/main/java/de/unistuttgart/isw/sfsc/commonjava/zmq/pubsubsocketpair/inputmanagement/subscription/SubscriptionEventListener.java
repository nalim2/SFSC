package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class SubscriptionEventListener {

  private final AtomicBoolean ready = new AtomicBoolean();
  private final ByteString topic;
  private final RemovingFuture future;

  SubscriptionEventListener(ByteString topic, RemovingFuture future) {
    this.topic = topic;
    this.future = future;
  }

  static <V> Future<V> addSubscriptionListener(SubscriptionTracker subscriptionTracker, ByteString topic, Callable<V> callable) {
    RemovingFuture<V> removingFuture = new RemovingFuture<>(callable);
    addSubscriptionListener(subscriptionTracker, topic, removingFuture);
    return removingFuture;
  }

  static <V> Future<V> addSubscriptionListener(SubscriptionTracker subscriptionTracker, ByteString topic, Runnable runnable, V result) {
    RemovingFuture<V> removingFuture = new RemovingFuture<>(runnable, result);
    addSubscriptionListener(subscriptionTracker, topic, removingFuture);
    return removingFuture;
  }

  static <V> Future<V> addUnsubscriptionListener(SubscriptionTracker subscriptionTracker, ByteString topic, Callable<V> callable) {
    RemovingFuture<V> removingFuture = new RemovingFuture<>(callable);
    addUnsubscriptionListener(subscriptionTracker, topic, removingFuture);
    return removingFuture;
  }

  static <V> Future<V> addUnsubscriptionListener(SubscriptionTracker subscriptionTracker, ByteString topic, Runnable runnable, V result) {
    RemovingFuture<V> removingFuture = new RemovingFuture<>(runnable, result);
    addUnsubscriptionListener(subscriptionTracker, topic, removingFuture);
    return removingFuture;
  }

  private static void addSubscriptionListener(SubscriptionTracker subscriptionTracker, ByteString topic, RemovingFuture removingFuture) {
    SubscriptionEventListener subscriptionEventListener = new SubscriptionEventListener(topic, removingFuture);
    Handle handle = subscriptionTracker.addSubscriptionListener(subscriptionEventListener::accept);
    subscriptionEventListener.initialize(handle);
    subscriptionTracker.getSubscriptions().forEach(subscriptionEventListener::accept);
  }

  private static void addUnsubscriptionListener(SubscriptionTracker subscriptionTracker, ByteString topic, RemovingFuture removingFuture) {
    SubscriptionEventListener subscriptionEventListener = new SubscriptionEventListener(topic, removingFuture);
    Handle handle = subscriptionTracker.addUnsubscriptionListener(subscriptionEventListener::accept);
    subscriptionEventListener.initialize(handle);
  }

  void initialize(Handle handle) {
    future.setHandle(handle);
    ready.set(true);
  }

  void accept(ByteString topic) {
    if (this.topic.equals(topic) && ready.compareAndSet(true, false)) {
      future.run();
    }
  }

  static class RemovingFuture<V> extends FutureTask<V> {

    private final AtomicReference<Handle> handle = new AtomicReference<>();

    RemovingFuture(Callable<V> callable) {
      super(callable);
    }

    RemovingFuture(Runnable runnable, V result) {
      super(runnable, result);
    }

    void setHandle(Handle handle){
      this.handle.compareAndSet(null, handle);
    }

    @Override
    protected void done() {
      handle.get().close();
    }
  }

}
