package de.unistuttgart.isw.sfsc.benchmark;

import de.unistuttgart.isw.sfsc.client.adapter.BootstrapConfiguration;

public class Main {

  public static void main(String[] args) {
    final BootstrapConfiguration clientConfiguration = new BootstrapConfiguration("127.0.0.1", 1251);
    final BootstrapConfiguration serverConfiguration = new BootstrapConfiguration("127.0.0.1", 1261);

    SfscBenchmark benchmark = new SfscBenchmark(clientConfiguration, serverConfiguration);
    Evaluator evaluator = new Evaluator();
    for (int i = 0; i < 10; i++) {
      evaluator.evaluate(benchmark.benchmark(5, 50, 1));
      evaluator.evaluate(benchmark.benchmark(500, 500, 5));
    }
  }
}
