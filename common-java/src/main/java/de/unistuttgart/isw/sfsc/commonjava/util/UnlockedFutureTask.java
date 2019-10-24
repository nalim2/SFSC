package de.unistuttgart.isw.sfsc.commonjava.util;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public final class UnlockedFutureTask<V> extends FutureTask<V> {

  public UnlockedFutureTask(Callable<V> callable) {
    super(callable);
  }

  public UnlockedFutureTask(Runnable runnable, V result) {
    super(runnable, result);
  }

  @Override
  public void setException(Throwable t) {
    super.setException(t);
  }


}
