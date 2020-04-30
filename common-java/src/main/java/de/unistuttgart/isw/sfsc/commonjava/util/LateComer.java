package de.unistuttgart.isw.sfsc.commonjava.util;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LateComer implements Runnable {

  private final AtomicReference<Runnable> reference = new AtomicReference<>();
  private final AtomicBoolean ready = new AtomicBoolean();

  @Override
  public void run() {
    ready.set(true);
    Optional.ofNullable(reference.get()).ifPresent(Runnable::run);
  }

  public void set(Runnable runnable) {
    reference.set(runnable);
    if (ready.get()) {
      runnable.run();
    }
  }


}
