package de.unistuttgart.isw.sfsc.commonjava.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueConnector<T> implements NotThrowingAutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(QueueConnector.class);
  private final ExecutorService executorService = Executors.newSingleThreadExecutor(new ExceptionLoggingThreadFactory(getClass().getName(), logger));
  private final BlockingSupplier<T> source;

  public QueueConnector(BlockingSupplier<T> source) {this.source = source;}

  public void start(Consumer<T> sink) {
    executorService.execute(() -> handleDataInboxLoop(source, sink));
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
    executorService.shutdownNow();
  }

  public interface BlockingSupplier<T> {

    T get() throws InterruptedException;
  }
}
