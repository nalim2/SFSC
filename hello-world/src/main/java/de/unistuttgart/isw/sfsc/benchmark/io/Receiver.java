package de.unistuttgart.isw.sfsc.benchmark.io;

import de.unistuttgart.isw.sfsc.benchmark.BenchmarkMessage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import protocol.pubsub.DataProtocol;
import zmq.reactor.ReactiveSocket.Inbox;

class Receiver implements AutoCloseable {

  private final ExecutorService executorService = Executors.newCachedThreadPool();
  private final Inbox inbox;
  private final Consumer<BenchmarkMessage> consumer;

  Receiver(Inbox inbox, Consumer<BenchmarkMessage> consumer) {
    this.inbox = inbox;
    this.consumer = consumer;
  }

  static Receiver start(Inbox inbox, Consumer<BenchmarkMessage> consumer) {
    Receiver receiver = new Receiver(inbox, consumer);
    receiver.start();
    return receiver;
  }

  void start() {
    executorService.execute(() -> {
      Thread.currentThread().setName("Benchmark Receiver Thread");
      while (!Thread.interrupted()) {
        try {
          final byte[][] message = inbox.take();
          executorService.execute(() -> {
            try {
              final long receiveTime = System.nanoTime();
              final BenchmarkMessage response = DataProtocol.PAYLOAD_FRAME.get(message, BenchmarkMessage.parser());
              final BenchmarkMessage finalResponse = BenchmarkMessage.newBuilder(response).setReceiveTimestamp(receiveTime).build();
              consumer.accept(finalResponse);
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
  }

  @Override
  public void close() {
    executorService.shutdownNow();
  }
}
