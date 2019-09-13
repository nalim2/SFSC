package de.unistuttgart.isw.sfsc.benchmark;

import de.unistuttgart.isw.sfsc.benchmark.evaluation.Evaluator;
import de.unistuttgart.isw.sfsc.benchmark.io.SfscBenchmark;
import de.unistuttgart.isw.sfsc.client.adapter.raw.BootstrapConfiguration;

public class Main {

  private static final BootstrapConfiguration clientConfiguration = new BootstrapConfiguration("127.0.0.1", 1251);
  private static final BootstrapConfiguration serverConfiguration = new BootstrapConfiguration("127.0.0.1", 1261);

  public static void main(String[] args) throws Exception {

    executeBenchmark(10,160);
    executeBenchmark(100,460);

  }

  static void executeBenchmark(int messagesPerSecond, int messageSizeBytes) throws Exception {
    Evaluator evaluator = new Evaluator();
    SfscBenchmark benchmark = new SfscBenchmark(clientConfiguration, serverConfiguration);
    benchmark.benchmark(messagesPerSecond, messageSizeBytes, 60, evaluator);
    evaluator.evaluate();
    evaluator.clear();
    System.out.println("=====================================================================");
    System.out.flush();
  }
}
