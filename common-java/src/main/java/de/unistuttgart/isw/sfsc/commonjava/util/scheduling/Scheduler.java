package de.unistuttgart.isw.sfsc.commonjava.util.scheduling;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface Scheduler {

  void execute(Runnable command);

  ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

  ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);
}
