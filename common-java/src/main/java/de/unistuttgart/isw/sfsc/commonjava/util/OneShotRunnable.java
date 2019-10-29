package de.unistuttgart.isw.sfsc.commonjava.util;

import java.util.concurrent.atomic.AtomicBoolean;

public final class OneShotRunnable implements Runnable {

  private final AtomicBoolean done = new AtomicBoolean();
  private final Runnable runnable;

  public OneShotRunnable(Runnable runnable) {this.runnable = runnable;}

  @Override
  public void run() {
    if (done.compareAndSet(false, true)) {
      runnable.run();
    }
  }
}
