package de.unistuttgart.isw.sfsc.benchmark.io;


import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.benchmark.BenchmarkMessage;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleClient;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.SchedulerService;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class Client implements NotThrowingAutoCloseable {

  private final SimpleClient client;
  private final SchedulerService schedulerService;
  private final Consumer<BenchmarkMessage> consumer;

  Client(PubSubConnection pubSubConnection, int threadPoolSize, Consumer<BenchmarkMessage> consumer) {
    schedulerService = new SchedulerService(threadPoolSize);
    this.consumer = consumer;
    client = new SimpleClient(pubSubConnection, SfscBenchmark.createTopic(), schedulerService);
  }

  void start(ByteString topic, int messageSizeBytes, long periodNs, int timeoutMs) {
    final MessageSupplier messageSupplier = new MessageSupplier(messageSizeBytes);
    schedulerService.scheduleAtFixedRate(
        () -> client.send(topic, messageSupplier.get(), this::accept, timeoutMs, () -> System.out.println("timeout")),
        0, periodNs, TimeUnit.NANOSECONDS);
  }

  void accept(ByteString bytes) {
    try {
      final long receiveTime = System.nanoTime();
      final BenchmarkMessage response = BenchmarkMessage.parseFrom(bytes);
      final BenchmarkMessage finalResponse = BenchmarkMessage.newBuilder(response).setReceiveTimestamp(receiveTime).build();
      consumer.accept(finalResponse);
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() {
    client.close();
    schedulerService.close();
  }
}
