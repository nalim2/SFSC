package de.unistuttgart.isw.sfsc.commonjava.util.scheduling;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MaxTimesRepetition implements NotThrowingAutoCloseable {

  private final AtomicInteger sendCounter = new AtomicInteger();
  private final AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>();

  private final Scheduler scheduler;
  private final Runnable task;
  private final int rateMs;
  private final int maxTries;

  MaxTimesRepetition(Scheduler scheduler, Runnable task, int rateMs, int maxTries) {
    this.scheduler = scheduler;
    this.task = task;
    this.rateMs = rateMs;
    this.maxTries = maxTries;
  }

  public static MaxTimesRepetition scheduleMaxTimes(Scheduler scheduler, Runnable task, int rateMs, int maxTries) {
    MaxTimesRepetition maxTimesRepetition = new MaxTimesRepetition(scheduler, task, rateMs, maxTries);
    maxTimesRepetition.start();
    return maxTimesRepetition;
  }

  void start() {
    future.set(scheduler.schedule(this::doIt, 0, TimeUnit.MILLISECONDS));
  }

  void doIt() {
    if (sendCounter.getAndIncrement() < maxTries) {
      task.run();
      if (sendCounter.get() < maxTries) {
        future.set(scheduler.schedule(this::doIt, rateMs, TimeUnit.MILLISECONDS));
      }
    }
  }


  @Override
  public void close() {
    sendCounter.set(maxTries);

    // we could have a race condition here. In this case, the task still gets scheduled again.
    // Although, in this case the if condition will be false, so the task wont be executed and we wont reschedule, therefore terminate at latest then
    // I consider this behaviour as sufficient.
    future.get().cancel(true);
  }
}
