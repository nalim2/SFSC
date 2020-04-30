package de.unistuttgart.isw.sfsc.commonjava.util.synchronizing;

import java.util.concurrent.TimeUnit;

public interface Awaitable {

  void await() throws InterruptedException;

  boolean await(long timeout, TimeUnit unit) throws InterruptedException;
}
