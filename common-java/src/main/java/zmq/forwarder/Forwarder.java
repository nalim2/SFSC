package zmq.forwarder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zmq.reactor.ReactiveSocket.Inbox;
import zmq.reactor.ReactiveSocket.Outbox;

public class Forwarder implements AutoCloseable {

  private static final Supplier<Integer> threadCounter = new AtomicInteger()::getAndIncrement;
  private static final Logger logger = LoggerFactory.getLogger(Forwarder.class);
  private final ExecutorService daemonExecutor = Executors.newSingleThreadExecutor();
  private final Inbox inbox;
  private final List<Outbox> outboxes;

  Forwarder(Inbox inbox, List<Outbox> outboxes) {
    this.inbox = inbox;
    this.outboxes = Collections.unmodifiableList(new ArrayList<>(outboxes));
  }

  public static Forwarder create(Inbox inbox, List<Outbox> outboxes) {
    Forwarder forwarder = new Forwarder(inbox, outboxes);
    forwarder.startDaemon();
    return forwarder;
  }

  public static  Forwarder create(Inbox inbox, Outbox outbox) {
    Forwarder forwarder = new Forwarder(inbox, List.of(outbox));
    forwarder.startDaemon();
    return forwarder;
  }

  void startDaemon() {
    daemonExecutor.execute(this::forward);
  }

  void forward() {
    Thread.currentThread().setName("Forwarding Thread " + threadCounter.get());
    while (!Thread.interrupted()) {
      try {
        byte[][] message = inbox.take();
        for (Outbox outbox : outboxes) {
          outbox.add(message);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        logger.warn("Unexpected Exception", e);
      }
    }
    logger.debug("{} finished!", Thread.currentThread().getName());
  }

  @Override
  public void close() {
    daemonExecutor.shutdownNow();
  }
}
