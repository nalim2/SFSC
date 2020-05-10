package de.unistuttgart.isw.sfsc.commonjava.util.synchronizing;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Synchronizer implements Runnable {

  private final CountDownLatch cdl = new CountDownLatch(1);

  @Override
  public void run() {
    cdl.countDown();
  }

  public Awaitable getAwaitable() {
    return new Awaitable() {
      @Override
      public void await() throws InterruptedException {
        cdl.await();
      }

      @Override
      public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return cdl.await(timeout, unit);
      }
    };
  }


}
