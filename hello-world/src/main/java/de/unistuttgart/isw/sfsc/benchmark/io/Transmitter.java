package de.unistuttgart.isw.sfsc.benchmark.io;

import de.unistuttgart.isw.sfsc.util.Util;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import zmq.reactor.ReactiveSocket.Outbox;

class Transmitter implements AutoCloseable {

  private final Outbox outbox;
  private final byte[] topic;
  private final MessageSupplier messageSupplier;
  private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(0);

  Transmitter(Outbox outbox, byte[] topic, int messageSizeBytes) {
    this.outbox = outbox;
    this.topic = topic;
    this.messageSupplier = new MessageSupplier(messageSizeBytes);
  }

  static void send(Outbox outbox, byte[] topic, byte[] data) {
    try {
      outbox.add(Util.dataMessage(topic, data));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void send(long periodNs) {
    scheduledExecutorService.scheduleAtFixedRate(() -> Transmitter.send(outbox, topic, messageSupplier.get()), 0, periodNs, TimeUnit.NANOSECONDS);
  }

  @Override
  public void close() {
    scheduledExecutorService.shutdownNow();
  }
}
