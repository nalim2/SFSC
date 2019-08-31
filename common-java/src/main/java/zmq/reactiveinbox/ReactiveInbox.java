package zmq.reactiveinbox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExceptionLoggingThreadFactory;
import zmq.reactor.ReactiveSocket.Inbox;

public class ReactiveInbox implements AutoCloseable {

  private static final Supplier<Integer> inboxCounter = new AtomicInteger()::getAndIncrement;
  private static final Logger logger = LoggerFactory.getLogger(ReactiveInbox.class);

  private final ExecutorService daemonExecutor = Executors
      .newSingleThreadExecutor(new ExceptionLoggingThreadFactory(getClass().getName() + "-" + inboxCounter.get(), logger));
  private final Inbox inbox;
  private final Consumer<byte[][]> processor;

  ReactiveInbox(Inbox inbox, Consumer<byte[][]> processor) {
    this.inbox = inbox;
    this.processor = processor;
  }

  public static ReactiveInbox create(Inbox inbox, Consumer<byte[][]> processor) {
    ReactiveInbox reactiveInbox = new ReactiveInbox(inbox, processor);
    reactiveInbox.startDaemon();
    return reactiveInbox;
  }

  void startDaemon() {
    daemonExecutor.submit(this::handleDataInboxLoop);
  }

  void handleDataInboxLoop() {
    while (!Thread.interrupted()) {
      try {
        byte[][] message = inbox.take();
        processor.accept(message);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    logger.debug("{} finished!", Thread.currentThread().getName());
  }

  @Override
  public void close() {
    daemonExecutor.shutdownNow();
  }
}
