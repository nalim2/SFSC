package de.unistuttgart.isw.sfsc.benchmark.evaluation;

import static java.util.stream.Collectors.toList;

import de.unistuttgart.isw.sfsc.benchmark.BenchmarkMessage;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

class BatchEvaluator {

  void evaluate(List<BenchmarkMessage> messages) {
    System.out.println("results");
    System.out.println("received " + messages.size() + " messages");
    if (messages.isEmpty()) {
      System.out.println("no messages received");
    } else {
      checkIdDistribution(messages);
      checkPing(messages);
    }
    System.out.println();
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
    for (long i = 1; i < maxId; i++) {
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

  void checkPing(List<BenchmarkMessage> messages) {

    double[] pings = messages.stream()
        .map(message -> message.getReceiveTimestamp() - message.getSendTimestamp())
        .mapToDouble(nsValue -> ((double) nsValue) / 1_000_000) //to ms
        .toArray();

    DescriptiveStatistics pingStats = new DescriptiveStatistics(pings);

    System.out.println();
    System.out.println(pingStats);
    System.out.println("percentile   1 " + pingStats.getPercentile(1));
    System.out.println("percentile  10 " + pingStats.getPercentile(10));
    System.out.println("percentile  20 " + pingStats.getPercentile(20));
    System.out.println("percentile  30 " + pingStats.getPercentile(30));
    System.out.println("percentile  40 " + pingStats.getPercentile(40));
    System.out.println("percentile  50 " + pingStats.getPercentile(50));
    System.out.println("percentile  60 " + pingStats.getPercentile(60));
    System.out.println("percentile  70 " + pingStats.getPercentile(70));
    System.out.println("percentile  80 " + pingStats.getPercentile(80));
    System.out.println("percentile  90 " + pingStats.getPercentile(90));
    System.out.println("percentile  95 " + pingStats.getPercentile(95));
    System.out.println("percentile  99 " + pingStats.getPercentile(99));
    System.out.println("percentile 100 " + pingStats.getPercentile(100));
  }
}
