package de.unistuttgart.isw.sfsc.benchmark.io;

import static de.unistuttgart.isw.sfsc.util.Util.pair;
import static java.util.concurrent.TimeUnit.SECONDS;

import de.unistuttgart.isw.sfsc.benchmark.BenchmarkMessage;
import de.unistuttgart.isw.sfsc.client.adapter.Adapter;
import de.unistuttgart.isw.sfsc.client.adapter.BootstrapConfiguration;
import java.util.function.Consumer;

public class SfscBenchmark {

  private static final int BENCHMARK_METADATA_SIZE_BYTES = 36;
  private static final int TOPIC_SIZE_BYTES = 4;

  private final int lingerDurationMs = 1000;

  private final BootstrapConfiguration serverConfiguration;
  private final BootstrapConfiguration clientConfiguration;

  public SfscBenchmark(BootstrapConfiguration clientConfiguration, BootstrapConfiguration serverConfiguration) {
    this.clientConfiguration = clientConfiguration;
    this.serverConfiguration = serverConfiguration;
  }

  public void benchmark(int messagesPerSecond, int messageSizeBytes, int measurementDurationSec, Consumer<BenchmarkMessage> consumer) throws Exception {
    printParameters(messagesPerSecond, messageSizeBytes, measurementDurationSec);
    final long periodNs = SECONDS.toNanos(1) / messagesPerSecond;
    System.out.println("initiating");

    try (final Adapter clientAdapter = Adapter.create(clientConfiguration);
        final Adapter serverAdapter = Adapter.create(serverConfiguration)) {

      final byte[] requestTopic = pair(clientAdapter, serverAdapter, TOPIC_SIZE_BYTES);
      final byte[] responseTopic = pair(serverAdapter, clientAdapter, TOPIC_SIZE_BYTES);

      try (final Server server = Server.start(serverAdapter, responseTopic);
          final Receiver receiver = Receiver.start(clientAdapter.getDataClient().getDataInbox(), consumer)) {
        try (final Transmitter transmitter = new Transmitter(clientAdapter.getDataClient().getDataOutbox(), requestTopic, messageSizeBytes)) {
          System.out.println("executing benchmark");
          Thread.sleep(lingerDurationMs); //some time to warm up
          transmitter.send(periodNs);
          System.out.println("please wait " + measurementDurationSec + " seconds");
          Thread.sleep(SECONDS.toMillis(measurementDurationSec));
        }

        Thread.sleep(lingerDurationMs); //wait for last messages to arrive
      }
    }
    System.out.println("Benchmark finished");
  }
    void printParameters ( int messagesPerSecond, int messageSizeBytes, int measurementDurationSec){
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
  }
