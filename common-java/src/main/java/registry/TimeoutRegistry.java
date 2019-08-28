package registry;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class TimeoutRegistry<K, V> implements AutoCloseable {

  private final ConcurrentHashMap<K, TimeoutContainer<V>> map = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(0);

 public void put(K key, V value, int timeoutMs, Consumer<Exception> exceptionConsumer) {
    ScheduledFuture<?> scheduledFuture = scheduledExecutorService.schedule(() -> {
          if (map.remove(key) != null) {
            exceptionConsumer.accept(new TimeoutException());
          }
        },
        timeoutMs,
        TimeUnit.MILLISECONDS);
    map.put(key, new TimeoutContainer<>(scheduledFuture, value));
  }

  public V remove(K key) {
    TimeoutContainer<V> container = map.remove(key);
    if (container != null) {
      container.timeoutRunnableFuture.cancel(true);
      return container.value;
    } else {
      return null;
    }
  }

  @Override
  public void close() {
    scheduledExecutorService.shutdownNow();
  }

  private static class TimeoutContainer<V> {

    private final ScheduledFuture<?> timeoutRunnableFuture;
    private final V value;

    private TimeoutContainer(ScheduledFuture<?> timeoutRunnableFuture, V value) {
      this.timeoutRunnableFuture = timeoutRunnableFuture;
      this.value = value;
    }
  }


}
