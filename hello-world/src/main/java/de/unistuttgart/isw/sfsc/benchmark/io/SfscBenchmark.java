package de.unistuttgart.isw.sfsc.benchmark.io;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.adapter.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.benchmark.BenchmarkMessage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class SfscBenchmark {

  private static final int BENCHMARK_METADATA_SIZE_BYTES = 36;
  private static final int TOPIC_SIZE_BYTES = 4;
  private static final int CLIENT_THREAD_POOL_SIZE = 2;
  private static final int SERVER_THREAD_POOL_SIZE = 2;

  private static final int lingerDurationMs = 10000;

  private final BootstrapConfiguration serverConfiguration;
  private final BootstrapConfiguration clientConfiguration;

  public SfscBenchmark(BootstrapConfiguration clientConfiguration, BootstrapConfiguration serverConfiguration) {
    this.clientConfiguration = clientConfiguration;
    this.serverConfiguration = serverConfiguration;
  }

  public void benchmark(int messagesPerSecond, int messageSizeBytes, int measurementDurationSec, Consumer<BenchmarkMessage> consumer)
      throws Exception {
    printParameters(messagesPerSecond, messageSizeBytes, measurementDurationSec);
    final long periodNs = SECONDS.toNanos(1) / messagesPerSecond;
    System.out.println("initiating");

    try (final Adapter clientAdapter = Adapter.create(clientConfiguration);
        final Adapter serverAdapter = Adapter.create(serverConfiguration)) {

      ByteString serverTopic = createTopic();

      try (final Server server = new Server(serverAdapter.dataConnection(), serverTopic, SERVER_THREAD_POOL_SIZE)) {
        try (final Client client = new Client(clientAdapter.dataConnection(), CLIENT_THREAD_POOL_SIZE, consumer)){
          System.out.println("executing benchmark");
          client.start(serverTopic, messageSizeBytes, periodNs, 100000);
          Thread.sleep(lingerDurationMs); //some time to warm up
          System.out.println("please wait " + measurementDurationSec + " seconds");
          Thread.sleep(SECONDS.toMillis(measurementDurationSec));
        }
        System.out.println("linger");
        Thread.sleep(lingerDurationMs); //wait for last messages to arrive
      }
      System.out.println("Benchmark finished");
    }
  }

  void printParameters(int messagesPerSecond, int messageSizeBytes, int measurementDurationSec) {
    final int totalMessageSize = messageSizeBytes + TOPIC_SIZE_BYTES + BENCHMARK_METADATA_SIZE_BYTES;
    System.out.println(
        "benchmark parameters: messagesPerSecond: " + messagesPerSecond + " messageSizeBytes: " + totalMessageSize + " measurementDurationSec: "
            + measurementDurationSec);
    System.out.println();
    System.out.println("sending message every " + SECONDS.toMicros(1) / messagesPerSecond + " µs");
    System.out.println("message amount ~" + messagesPerSecond * measurementDurationSec);
    System.out.println("payload throughput is approximately " + messagesPerSecond * totalMessageSize + " bytes/sec");
    System.out.println();
  }

  static ByteString createTopic() {
    byte[] topicBytes = new byte[TOPIC_SIZE_BYTES];
    ThreadLocalRandom.current().nextBytes(topicBytes);
    return ByteString.copyFrom(topicBytes);

  }
}
