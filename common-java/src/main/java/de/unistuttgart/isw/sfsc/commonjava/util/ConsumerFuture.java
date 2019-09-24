package de.unistuttgart.isw.sfsc.commonjava.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConsumerFuture<T, U> implements Consumer<T>, Future<U> {

  private final AtomicReference<T> reference = new AtomicReference<>();
  private final FutureTask<U> future;

  public ConsumerFuture(Function<T, U> converter) {
    future = new FutureTask<>(()-> converter.apply(reference.get()));
  }

  @Override
  public void accept(T data) {
    reference.set(data);
    future.run();
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return future.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return future.isCancelled();
  }

  @Override
  public boolean isDone() {
    return future.isDone();
  }

  @Override
  public U get() throws InterruptedException, ExecutionException {
    return future.get();
  }

  @Override
  public U get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return future.get(timeout, unit);
  }
}
