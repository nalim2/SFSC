package de.unistuttgart.isw.sfsc.benchmark.io;

import com.google.protobuf.Message;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExceptionLoggingThreadFactory;
import zmq.pubsubsocketpair.PubSubConnection.Publisher;

class Transmitter implements AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(Transmitter.class);
  private final ScheduledExecutorService scheduledExecutorService = Executors
      .newScheduledThreadPool(0, new ExceptionLoggingThreadFactory("BenchmarkTransmitter", logger));
  private final Publisher publisher;
  private final byte[] topic;
  private final MessageSupplier messageSupplier;

  Transmitter(Publisher publisher, byte[] topic, int messageSizeBytes) {
    this.publisher = publisher;
    this.topic = topic;
    this.messageSupplier = new MessageSupplier(messageSizeBytes);
  }

  void send(long periodNs) {
    scheduledExecutorService.scheduleAtFixedRate(() -> Transmitter.send(publisher, topic, messageSupplier.get()), 0, periodNs, TimeUnit.NANOSECONDS);
  }

  static void send(Publisher publisher, byte[] topic, Message data) {
    publisher.publish(topic, data);
  }

  @Override
  public void close() {
    scheduledExecutorService.shutdownNow();
  }
}
