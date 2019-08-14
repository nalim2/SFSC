package de.unistuttgart.isw.sfsc.benchmark.evaluation;

import de.unistuttgart.isw.sfsc.benchmark.BenchmarkMessage;
import java.util.function.Consumer;

public class Evaluator implements Consumer<BenchmarkMessage> {

  private final Collector collector = new Collector();
  private final StreamEvaluator streamEvaluator = new StreamEvaluator();

  @Override
  public void accept(BenchmarkMessage message) {
    streamEvaluator.accept(message);
    collector.accept(message);
  }

  public void evaluate() {
    new BatchEvaluator().evaluate(collector.getMessages());
  }

  public void clear(){
    collector.clear();
  }
}
