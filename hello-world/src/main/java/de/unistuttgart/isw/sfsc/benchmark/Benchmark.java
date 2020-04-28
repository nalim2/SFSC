package de.unistuttgart.isw.sfsc.benchmark;

import de.unistuttgart.isw.sfsc.adapter.configuration.AdapterConfiguration;
import de.unistuttgart.isw.sfsc.benchmark.evaluation.Evaluator;
import de.unistuttgart.isw.sfsc.benchmark.io.SfscBenchmark;

public class Benchmark {

  private static final AdapterConfiguration clientConfiguration = new AdapterConfiguration().setCorePort(1251);
  private static final AdapterConfiguration serverConfiguration = new AdapterConfiguration().setCorePort(1261);

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
