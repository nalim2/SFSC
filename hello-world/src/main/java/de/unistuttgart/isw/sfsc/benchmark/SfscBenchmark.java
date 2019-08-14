package de.unistuttgart.isw.sfsc.benchmark;

import static de.unistuttgart.isw.sfsc.util.Util.subscriptionMessage;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.client.adapter.Adapter;
import de.unistuttgart.isw.sfsc.client.adapter.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.util.Util;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import protocol.pubsub.DataProtocol;
import zmq.reactor.ReactiveSocket.Inbox;
import zmq.reactor.ReactiveSocket.Outbox;

class SfscBenchmark {

  private static final int BENCHMARK_METADATA_SIZE_BYTES = 36;

  private final int rampUpDurationMs = 100;
  private final int lingerDurationMs = 100;
  private final int topicSizeBytes = 4;

  private final BootstrapConfiguration serverConfiguration;
  private final BootstrapConfiguration clientConfiguration;

  SfscBenchmark(BootstrapConfiguration clientConfiguration, BootstrapConfiguration serverConfiguration) {
    this.clientConfiguration = clientConfiguration;
    this.serverConfiguration = serverConfiguration;
  }

  Collection<BenchmarkMessage> benchmark(int messagesPerSecond, int messageSizeBytes, int measurementDurationSec) {
    printParameters(messagesPerSecond, messageSizeBytes, measurementDurationSec);
    final long periodNs = SECONDS.toNanos(1) / messagesPerSecond;
    final Queue<BenchmarkMessage> messages = new ConcurrentLinkedQueue<>();
    final ScheduledExecutorService senderExecutorService = Executors.newScheduledThreadPool(1);
    final ExecutorService executorService = Executors.newCachedThreadPool();
    final long rampMessages = MILLISECONDS.toSeconds(messagesPerSecond * rampUpDurationMs);
    System.out.println("initiating");
    try (final Adapter clientAdapter = Adapter.create(clientConfiguration); final Adapter serverAdapter = Adapter.create(serverConfiguration)) {
      final byte[] requestTopic = pair(clientAdapter, serverAdapter);
      final byte[] responseTopic = pair(serverAdapter, clientAdapter);
      final AtomicLong idGenerator = new AtomicLong(-rampMessages);

      //sender
      senderExecutorService.scheduleAtFixedRate(() -> {
        try {
          final Outbox outbox = clientAdapter.getDataClient().getDataOutbox();
          final byte[] weightBytes = new byte[messageSizeBytes];
          ThreadLocalRandom.current().nextBytes(weightBytes);
          final ByteString weightByteString = ByteString.copyFrom(weightBytes);
          final BenchmarkMessage request = BenchmarkMessage.newBuilder().setWeight(weightByteString).setId(idGenerator.getAndIncrement())
              .setSendTime(System.nanoTime()).setProcessTime(Long.MAX_VALUE).setReturnTime(Long.MAX_VALUE).build();
          outbox.add(Util.dataMessage(requestTopic, request));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }, 0, periodNs, TimeUnit.NANOSECONDS);

      //server
      executorService.execute(() -> {
        final Inbox inbox = serverAdapter.getDataClient().getDataInbox();
        final Outbox outbox = serverAdapter.getDataClient().getDataOutbox();
        while (!Thread.interrupted()) {
          try {
            final byte[][] message = inbox.take();
            executorService.execute(() -> {
              try {
                final BenchmarkMessage request = DataProtocol.PAYLOAD_FRAME.get(message, BenchmarkMessage.parser());
                final BenchmarkMessage response = BenchmarkMessage.newBuilder(request).setProcessTime(System.nanoTime()).build();
                outbox.add(Util.dataMessage(responseTopic, response));
              } catch (Exception e) {
                e.printStackTrace();
              }
            });
          } catch (InterruptedException | RejectedExecutionException e) {
            Thread.currentThread().interrupt();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });

      //receiver
      executorService.execute(() -> {
        final Inbox inbox = clientAdapter.getDataClient().getDataInbox();
        while (!Thread.interrupted()) {
          try {
            final byte[][] message = inbox.take();
            executorService.execute(() -> {
              try {
                final BenchmarkMessage response = DataProtocol.PAYLOAD_FRAME.get(message, BenchmarkMessage.parser());
                final long receiveTime = System.nanoTime();
                final BenchmarkMessage finalResponse = BenchmarkMessage.newBuilder(response).setReturnTime(receiveTime).build();
                messages.add(finalResponse);
              } catch (Exception e) {
                e.printStackTrace();
              }
            });
          } catch (InterruptedException | RejectedExecutionException e) {
            Thread.currentThread().interrupt();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });

      //wait for messages
      System.out.println("executing benchmark");
      System.out.println("please wait " + measurementDurationSec + " seconds");
      Thread.sleep(rampUpDurationMs + SECONDS.toMillis(measurementDurationSec));
      //stop when time is over
      senderExecutorService.shutdownNow();
      //wait for last messages to arrive
      Thread.sleep(lingerDurationMs);
      executorService.shutdownNow();
      System.out.println();
      System.out.flush();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      senderExecutorService.shutdownNow();
      executorService.shutdownNow();
    }
    return messages;
  }

  byte[] pair(Adapter sender, Adapter receiver) throws InterruptedException {
    byte[] topic = new byte[topicSizeBytes];
    ThreadLocalRandom.current().nextBytes(topic);
    receiver.getDataClient().getSubEventOutbox().add(subscriptionMessage(topic));
    sender.getDataClient().getSubEventInbox().take();
    return topic;
  }

  void printParameters(int messagesPerSecond, int messageSizeBytes, int measurementDurationSec) {
    final int totalMessageSize = messageSizeBytes + topicSizeBytes + BENCHMARK_METADATA_SIZE_BYTES;
    System.out.println(
        "benchmark parameters: messagesPerSecond: " + messagesPerSecond + " messageSizeBytes: " + messageSizeBytes + " measurementDurationSec: "
            + measurementDurationSec);
    System.out.println("rampUpDurationMs: " + rampUpDurationMs + " lingerDurationMs " + lingerDurationMs);
    System.out.println();
    System.out.println("total message size is " + totalMessageSize);
    System.out.println("sending message every " + SECONDS.toMicros(1) / messagesPerSecond + " µs");
    System.out.println("message amount ~" + messagesPerSecond * measurementDurationSec);
    System.out.println("total throughput is approximately " + messagesPerSecond * totalMessageSize + " bytes/sec");
    System.out.println();
  }
}
