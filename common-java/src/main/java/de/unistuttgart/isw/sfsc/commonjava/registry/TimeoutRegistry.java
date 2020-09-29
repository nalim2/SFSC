package de.unistuttgart.isw.sfsc.commonjava.registry;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.SchedulerService;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TimeoutRegistry<K, V> implements NotThrowingAutoCloseable {


  private static final int THREAD_NUMBER = 1;

  private final ConcurrentHashMap<K, TimeoutContainer<V>> map = new ConcurrentHashMap<>();
  private final SchedulerService schedulerService = new SchedulerService(THREAD_NUMBER);

  public void put(K key, V value, int timeoutMs, Runnable timeoutRunnable) {
    AtomicReference<ScheduledFuture<?>> scheduledFuture = new AtomicReference<>();
    map.put(key, new TimeoutContainer<>(scheduledFuture, value));
    scheduledFuture.set(schedulerService.schedule(() -> {
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
    schedulerService.close();
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
