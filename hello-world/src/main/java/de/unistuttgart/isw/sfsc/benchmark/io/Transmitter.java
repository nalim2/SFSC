package de.unistuttgart.isw.sfsc.benchmark.io;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import zmq.pubsubsocketpair.PubSubSocketPair.Publisher;

class Transmitter implements AutoCloseable {

  private final Publisher publisher;
  private final byte[] topic;
  private final MessageSupplier messageSupplier;
  private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(0);

  Transmitter(Publisher publisher, byte[] topic, int messageSizeBytes) {
    this.publisher = publisher;
    this.topic = topic;
    this.messageSupplier = new MessageSupplier(messageSizeBytes);
  }

  static void send(Publisher publisher, byte[] topic, byte[] data) {
    try {
      publisher.publish(topic, data);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void send(long periodNs) {
    scheduledExecutorService.scheduleAtFixedRate(() -> Transmitter.send(publisher, topic, messageSupplier.get()), 0, periodNs, TimeUnit.NANOSECONDS);
  }

  @Override
  public void close() {
    scheduledExecutorService.shutdownNow();
  }
}
