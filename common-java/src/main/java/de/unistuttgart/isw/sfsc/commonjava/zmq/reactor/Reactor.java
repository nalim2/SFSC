package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor;

import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import java.util.concurrent.ExecutionException;

public interface Reactor extends NotThrowingAutoCloseable {

  ReactiveSocket createSubscriber() throws ExecutionException, InterruptedException;

  ReactiveSocket createPublisher() throws ExecutionException, InterruptedException;

  Handle addShutdownListener(Runnable runnable);

  @Override
  void close();
}
