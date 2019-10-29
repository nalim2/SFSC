package de.unistuttgart.isw.sfsc.commonjava.util;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class OneShotListener<T> implements Consumer<T> {

  private final AtomicBoolean ready = new AtomicBoolean();
  private final Predicate<T> predicate;
  private final RemovingFuture future;

  public OneShotListener(Predicate<T> predicate, Runnable runnable) {
    this.predicate = predicate;
    this.future = new RemovingFuture(runnable);
  }

  public Future<Void> initialize(Handle handle) {
    future.setHandle(handle);
    ready.set(true);
    return future;
  }

  public void accept(T element) {
    if (predicate.test(element) && ready.compareAndSet(true, false)) {
      future.run();
    }
  }

  static class RemovingFuture extends FutureTask<Void> {

    private final AtomicReference<Handle> handle = new AtomicReference<>();

    RemovingFuture(Runnable runnable) {
      super(runnable, null);
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
