package de.unistuttgart.isw.sfsc.commonjava.util;

import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class DeadMansSwitch implements NotThrowingAutoCloseable {

  private final AtomicBoolean closed = new AtomicBoolean();
  private final AtomicReference<Future<?>> runnableFuture = new AtomicReference<>();
  private final Listeners<Runnable> runnables = new Listeners<>();
  private final Scheduler scheduler;
  private final int rateMs;

  DeadMansSwitch(Scheduler scheduler, int rateMs) {
    this.scheduler = scheduler;
    this.rateMs = rateMs;
  }

  public static DeadMansSwitch create(Scheduler scheduler, int rateMs) {
    DeadMansSwitch deadMansSwitch = new DeadMansSwitch(scheduler, rateMs);
    deadMansSwitch.restart();
    return deadMansSwitch;
  }

  public Handle addOnDeceaseListener(Runnable runnable) {
    return runnables.add(runnable);
  }

  public void keepAlive() {
    runnableFuture.get().cancel(true);
    restart();
  }

  void restart() {
    runnableFuture.set(scheduler.schedule(() ->
        {
          close();
          runnables.forEach(Runnable::run);
        },
        rateMs, TimeUnit.MILLISECONDS)
    );
    if (closed.get()) {
      runnableFuture.get().cancel(true);
    }
  }

  @Override
  public void close() {
    closed.set(true);
    runnableFuture.get().cancel(false);
  }
}
