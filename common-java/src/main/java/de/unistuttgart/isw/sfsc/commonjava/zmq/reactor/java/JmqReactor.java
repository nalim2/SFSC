package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.java;

import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import java.util.concurrent.ExecutionException;
import org.zeromq.ZContext;

public class JmqReactor implements Reactor {

  private final JmqExecutor executor;

  public static JmqReactor create() throws InterruptedException {
    ZContext zContext = new ZContext();
    zContext.setRcvHWM(0);
    zContext.setSndHWM(0);
    return new JmqReactor(zContext);
  }

  JmqReactor(ZContext zContext) throws InterruptedException {
    executor = JmqExecutor.create(zContext);
  }

  @Override
  public ReactiveSocket createSubscriber() throws ExecutionException, InterruptedException {
    return executor.createSubscriber().get(); //todo wait
  }

  @Override
  public ReactiveSocket createPublisher() throws ExecutionException, InterruptedException {
    return executor.createPublisher().get(); //todo wait
  }

  @Override
  public Handle addShutdownListener(Runnable runnable) {
    return executor.addShutdownListener(runnable);
  }

  @Override
  public void close() {
    executor.close();
  }
}
