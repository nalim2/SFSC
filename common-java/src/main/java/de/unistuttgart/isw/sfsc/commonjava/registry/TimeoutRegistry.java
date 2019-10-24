package de.unistuttgart.isw.sfsc.commonjava.registry;

import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutRegistry<K, V> implements NotThrowingAutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(TimeoutRegistry.class);

  private final ConcurrentHashMap<K, TimeoutContainer<V>> map = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduledExecutorService = Executors
      .newScheduledThreadPool(0, new ExceptionLoggingThreadFactory(getClass().getName(), logger));

  public void put(K key, V value, int timeoutMs, Runnable timeoutRunnable) {
    AtomicReference<ScheduledFuture<?>> scheduledFuture = new AtomicReference<>();
    map.put(key, new TimeoutContainer<>(scheduledFuture, value));
    scheduledFuture.set(scheduledExecutorService.schedule(() -> {
          if (map.remove(key) != null) {
            timeoutRunnable.run();
          }
        },
        timeoutMs,
        TimeUnit.MILLISECONDS));
  }

  public Optional<V> remove(K key) {
    TimeoutContainer<V> container = map.remove(key);
    if (container != null) {
      Optional.ofNullable(container.timeoutFuture.get()).ifPresent(future -> future.cancel(true));
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

    private final AtomicReference<ScheduledFuture<?>> timeoutFuture;
    private final V value;

    private TimeoutContainer(AtomicReference<ScheduledFuture<?>> timeoutFuture, V value) {
      this.timeoutFuture = timeoutFuture;
      this.value = value;
    }
  }


}
