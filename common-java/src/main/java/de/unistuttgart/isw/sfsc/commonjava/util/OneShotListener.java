package de.unistuttgart.isw.sfsc.commonjava.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class OneShotListener<T, U> implements Consumer<T> {

  private final AtomicBoolean ready = new AtomicBoolean();
  private final Predicate<T> predicate;
  private final RemovingFuture<U> future;

  public OneShotListener(Predicate<T> predicate, Callable<U> callable) {
    this.predicate = predicate;
    this.future = new RemovingFuture<>(callable);
  }

  public Future<U> initialize(Handle handle) {
    future.setHandle(handle);
    ready.set(true);
    return future;
  }

  public void accept(T element) {
    if (predicate.test(element) && ready.compareAndSet(true, false)) {
      future.run();
    }
  }

  static class RemovingFuture<V> extends FutureTask<V> {

    private final AtomicReference<Handle> handle = new AtomicReference<>();

    RemovingFuture(Callable<V> callable) {
      super(callable);
    }

    void setHandle(Handle handle) {
      this.handle.compareAndSet(null, handle);
    }

    @Override
    protected void done() {
      handle.get().close();
    }
  }

}
