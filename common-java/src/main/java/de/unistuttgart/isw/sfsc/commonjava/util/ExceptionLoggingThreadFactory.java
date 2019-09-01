package de.unistuttgart.isw.sfsc.commonjava.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.slf4j.Logger;

public class ExceptionLoggingThreadFactory implements ThreadFactory {

  private final ThreadGroup threadGroup;
  private final Supplier<String> nameSupplier;
  private final UncaughtExceptionHandler uncaughtExceptionHandler;

  public ExceptionLoggingThreadFactory(String threadGroupName, Logger logger) {
    AtomicInteger number = new AtomicInteger();
    nameSupplier = () -> threadGroupName + "-thread-" + number.getAndIncrement();
    threadGroup = new ThreadGroup(threadGroupName);
    uncaughtExceptionHandler = (t, e) -> logger.warn("Uncaught Exception in thread " + t.getName(), e);
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread thread = new Thread(threadGroup, r, nameSupplier.get());
    thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
    return thread;
  }

}
