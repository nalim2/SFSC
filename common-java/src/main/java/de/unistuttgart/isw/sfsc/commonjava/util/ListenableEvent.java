package de.unistuttgart.isw.sfsc.commonjava.util;

import java.util.concurrent.atomic.AtomicBoolean;

public class ListenableEvent {

  private final Listeners<Runnable> listeners = new Listeners<>();
  private final AtomicBoolean fired = new AtomicBoolean();

  public Handle addListener(Runnable runnable) {
    LateComer lateComer = new LateComer();
    Handle handle = listeners.add(lateComer);
    lateComer.set(new OneShotRunnable(() -> {
      runnable.run();
      handle.close();
    }));
    if (fired.get()) {
      lateComer.run();
    }
    return handle;
  }

  public void fire() {
    fired.set(true);
    listeners.forEach(Runnable::run);
  }

  public boolean isFired() {
    return fired.get();
  }
}
