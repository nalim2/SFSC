package de.unistuttgart.isw.sfsc.commonjava.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MaxTimesRepetition implements AutoCloseable {

  private AtomicBoolean stopped = new AtomicBoolean();
  private final AtomicInteger sendCounter = new AtomicInteger();

  private final ScheduledExecutorService scheduledExecutorService;
  private final Runnable task;
  private final int rateMs;
  private final int maxTries;

  MaxTimesRepetition(ScheduledExecutorService scheduledExecutorService, Runnable task, int rateMs, int maxTries) {
    this.scheduledExecutorService = scheduledExecutorService;
    this.task = task;
    this.rateMs = rateMs;
    this.maxTries = maxTries;
  }

  public static MaxTimesRepetition scheduleMaxTimes(ScheduledExecutorService scheduledExecutorService, Runnable task, int rateMs, int maxTries) {
    MaxTimesRepetition maxTimesRepetition = new MaxTimesRepetition(scheduledExecutorService, task, rateMs, maxTries);
    maxTimesRepetition.doIt();
    return maxTimesRepetition;
  }

  void doIt() {
    if (!stopped.get() && sendCounter.getAndIncrement() <= maxTries) {
      scheduledExecutorService.schedule(this::doIt, rateMs, TimeUnit.MILLISECONDS);
      task.run();
    }
  }

  @Override
  public void close() {
    stopped.set(true);
  }
}
