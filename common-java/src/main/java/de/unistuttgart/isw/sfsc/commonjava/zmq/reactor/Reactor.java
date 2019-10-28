package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import java.util.concurrent.ExecutionException;
import org.zeromq.ZContext;

public class Reactor implements NotThrowingAutoCloseable {

  private final ZmqExecutor executor;

  Reactor(ZContext zContext) throws InterruptedException {
    executor = ZmqExecutor.create(zContext);
  }

  public static Reactor create(ContextConfiguration contextConfiguration) throws InterruptedException {
    ZContext zContext = new ZContext();
    contextConfiguration.configure(zContext);
    return new Reactor(zContext);
  }

  public ReactiveSocket createSubscriber() throws ExecutionException, InterruptedException {
    return executor.createSubscriber().get(); //todo wait
  }

  public ReactiveSocket createPublisher() throws ExecutionException, InterruptedException {
    return executor.createPublisher().get(); //todo wait
  }

  @Override
  public void close() {
    executor.close();
  }
}
