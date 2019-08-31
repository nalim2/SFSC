package zmq.reactor;

import java.util.concurrent.ExecutionException;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

public class Reactor implements AutoCloseable {

  private final ZMQWorker worker;

  Reactor(ZContext zContext) throws InterruptedException {
    worker = ZMQWorker.create(zContext);
  }

  public static Reactor create(ContextConfiguration contextConfiguration) throws InterruptedException {
    ZContext zContext = new ZContext();
    contextConfiguration.configure(zContext);
    return new Reactor(zContext);
  }

  public <InboxT extends Enum<InboxT>> ReactiveSocket createReactiveSocket(SocketType type, Class<InboxT> inboxProtocol)
      throws ExecutionException, InterruptedException {
    QueuingHandler queuingHandler = QueuingHandler.forProtocol(inboxProtocol);
    return worker.createSocket(type, queuingHandler, queuingHandler.getInbox()).get();
  }

  @Override
  public void close() {
    worker.close();
  }
}
