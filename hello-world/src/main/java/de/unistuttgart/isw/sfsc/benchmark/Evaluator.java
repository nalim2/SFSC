package de.unistuttgart.isw.sfsc.benchmark;

import static java.util.stream.Collectors.toList;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Evaluator {

  void evaluate(Collection<BenchmarkMessage> benchmarkMessages) {
    System.out.println("results");
    Collection<BenchmarkMessage> messages = preprocess(benchmarkMessages);
    if (messages.isEmpty()) {
      System.out.println("no messages received");
    } else {
      System.out.println("total messages received: " + messages.size());
      checkIdDistribution(messages);
      checkPing(messages);
    }
    System.out.println();
  }

  List<BenchmarkMessage> preprocess(Collection<BenchmarkMessage> messages) {
    return messages.stream().filter(message -> message.getId() >= 0).collect(Collectors.toList());
  }

  void checkIdDistribution(Collection<BenchmarkMessage> messages) {

    //id -> amount received
    Map<Long, Long> inventory = messages.stream()
        .collect(Collectors.groupingBy(BenchmarkMessage::getId, Collectors.counting()));

    //missing ids between 0 and highest id
    final long maxId = messages.stream()
        .mapToLong(BenchmarkMessage::getId)
        .max()
        .orElse(0);
    for (long i = 0; i < maxId; i++) {
      inventory.putIfAbsent(i, 0L);
    }

    // frequency of amount received
    // times received -> list with Ids
    Map<Long, List<Long>> frequency = inventory.entrySet().stream()
        .map(entry -> new SimpleImmutableEntry<>(entry.getValue(), entry.getKey()))
        .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, toList())));

    // times received -> amount of Ids
    Map<Long, Integer> frequencyAmount = frequency.entrySet().stream()
        .map(entry -> new SimpleImmutableEntry<>(entry.getKey(), entry.getValue().size()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    // diverging ids
    Map<Long, List<Long>> deviators = new HashMap<>(frequency);
    deviators.remove(1L);

    if (deviators.size() != 0) {
      System.out.println("frequency distribution: (x times received = message amount) " + frequencyAmount.toString());
      System.out.println("deviators (x times received = message Ids): " + deviators.toString());
    } else {
      System.out.println("every sent message received exactly 1 time");
    }
  }

  void checkPing(Collection<BenchmarkMessage> messages) {

    List<Long> pingNsList = new ArrayList<>(messages.size());
    messages.forEach(message -> pingNsList.add(message.getReturnTime() - message.getSendTime()));

    DoubleSummaryStatistics pingSummary = pingNsList.stream()
        .map(x -> (double) x)
        .map(nsValue -> nsValue / 1_000_000)  //ns to ms
        .collect(Collectors.summarizingDouble(x -> x));

    System.out.println("average ping " + pingSummary.getAverage() + " ms");
    System.out.println("max ping " + pingSummary.getMax() + " ms");
    System.out.println("min ping " + pingSummary.getMin() + " ms");
  }
}
