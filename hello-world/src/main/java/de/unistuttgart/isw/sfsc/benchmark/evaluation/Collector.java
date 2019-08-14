package de.unistuttgart.isw.sfsc.benchmark.evaluation;

import de.unistuttgart.isw.sfsc.benchmark.BenchmarkMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

class Collector implements Consumer<BenchmarkMessage> {

  private final Queue<BenchmarkMessage> queue = new ConcurrentLinkedQueue<>();

  @Override
  public void accept(BenchmarkMessage message) {
    queue.add(message);
  }

  List<BenchmarkMessage> getMessages() {
    return new ArrayList<>(queue);
  }

  void clear(){
    queue.clear();
  }
}
