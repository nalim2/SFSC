package de.unistuttgart.isw.sfsc.commonjava.util.scheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SchedulerService implements Scheduler, AutoCloseable {

  private final ScheduledExecutorService scheduledExecutorService;

  public SchedulerService(int numberOfThreads) {
    scheduledExecutorService = Executors.newScheduledThreadPool(numberOfThreads);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    return scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period, unit);
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    return scheduledExecutorService.schedule(command, delay, unit);
  }

  @Override
  public void execute(Runnable command) {
    scheduledExecutorService.execute(command);
  }

  @Override
  public void close() {
    scheduledExecutorService.shutdownNow();
  }
}
