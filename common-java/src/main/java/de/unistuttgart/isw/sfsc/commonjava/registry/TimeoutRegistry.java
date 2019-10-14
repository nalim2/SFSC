package de.unistuttgart.isw.sfsc.commonjava.registry;

import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutRegistry<K, V> implements AutoCloseable {

  public static final int DEFAULT_TIMEOUT_MS = 500;

  private static final Logger logger = LoggerFactory.getLogger(TimeoutRegistry.class);

  private final ConcurrentHashMap<K, TimeoutContainer<V>> map = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduledExecutorService = Executors
      .newScheduledThreadPool(0, new ExceptionLoggingThreadFactory(getClass().getName(), logger));

  public void put(K key, V value, int timeoutMs, Runnable timeoutRunnable) {
    ScheduledFuture<?> scheduledFuture = scheduledExecutorService.schedule(() -> {
          if (map.remove(key) != null) {
            timeoutRunnable.run();
          }
        },
        timeoutMs,
        TimeUnit.MILLISECONDS);
    //if timeout is like really really low, we could run in a race condition here, since even is fired before we put the container into the map
    //although, in this use case the timeout will not be that low so I consider this as safe
    map.put(key, new TimeoutContainer<>(scheduledFuture, value));
  }

  public Optional<V> remove(K key) {
    TimeoutContainer<V> container = map.remove(key);
    if (container != null) {
      container.timeoutRunnableFuture.cancel(true);
      return Optional.of(container.value);
    } else {
      return Optional.empty();
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
