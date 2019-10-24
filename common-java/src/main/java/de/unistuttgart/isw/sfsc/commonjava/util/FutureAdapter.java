package de.unistuttgart.isw.sfsc.commonjava.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class FutureAdapter<T, U> implements Future<U> {

  private final AtomicReference<T> reference = new AtomicReference<>();
  private final FutureTask<U> future;

  public FutureAdapter(ThrowingFunction<T, U> converter, Callable<U> errorHandler) {
    future = new FutureTask<>(() -> {
      T input = reference.get();
      if (input != null) {
        return converter.apply(reference.get());
      } else {
        return errorHandler.call();
      }
    });
  }

  public void handleInput(T input) {
    reference.set(input);
    future.run();
  }

  public void handleError() {
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

  public interface ThrowingFunction<T, U> {

    U apply(T t) throws Exception;
  }
}
