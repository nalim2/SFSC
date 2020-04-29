package de.unistuttgart.isw.sfsc.commonjava.util.listening;

import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import java.util.Optional;
import java.util.Queue;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ListenableQueue<T> implements Consumer<T>, Supplier<T> {

  private final AtomicReference<Runnable> listener = new AtomicReference<>();
  private final Queue<T> queue = new ConcurrentLinkedQueue<>();

  public Handle addListener(Runnable runnable) {
    if (!listener.compareAndSet(null, runnable)) {
      runnable.run();
    } else {
      throw new RuntimeException(new TooManyListenersException());
    }
    return () -> listener.compareAndSet(runnable, null);
  }

  @Override
  public T get() {
    return queue.poll();
  }

  @Override
  public void accept(T t) {
    queue.add(t);
    Optional.ofNullable(listener.get()).ifPresent(Runnable::run);
  }
}

