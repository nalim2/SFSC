package consumerfuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConsumerFuture<T, U> implements Consumer<T>, Future<U> {

  private volatile U output;
  private final FutureTask<U> future = new FutureTask<>(() -> output);
  private final Function<T, U> converter;

  public ConsumerFuture(Function<T, U> converter) {
    this.converter = converter;
  }

  @Override
  public void accept(T input) {
    output = converter.apply(input);
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
