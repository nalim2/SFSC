package de.unistuttgart.isw.sfsc.commonjava.util;

import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.SchedulerService;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueConnector<T> implements NotThrowingAutoCloseable {
  private static final int THREAD_NUMBER = 1;

  private static final Logger logger = LoggerFactory.getLogger(QueueConnector.class);
  private final SchedulerService schedulerService = new SchedulerService(THREAD_NUMBER);
  private final BlockingSupplier<T> source;

  public QueueConnector(BlockingSupplier<T> source) {
    this.source = source;
  }

  public void start(Consumer<T> sink) {
    schedulerService.execute(() -> handleDataInboxLoop(source, sink));
  }

  void handleDataInboxLoop(BlockingSupplier<T> source, Consumer<T> sink) {
    while (!Thread.interrupted()) {
      try {
        T element = source.get();
        sink.accept(element);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    logger.debug("{} finished!", Thread.currentThread().getName());
  }

  @Override
  public void close() {
    schedulerService.close();
  }

  public interface BlockingSupplier<T> {

    T get() throws InterruptedException;
  }
}
