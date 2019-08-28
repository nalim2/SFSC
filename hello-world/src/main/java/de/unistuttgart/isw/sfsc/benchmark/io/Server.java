package de.unistuttgart.isw.sfsc.benchmark.io;

import de.unistuttgart.isw.sfsc.benchmark.BenchmarkMessage;
import de.unistuttgart.isw.sfsc.client.adapter.RawAdapter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import protocol.pubsub.DataProtocol;
import zmq.pubsubsocketpair.PubSubSocketPair.Publisher;
import zmq.reactor.ReactiveSocket.Inbox;

class Server implements AutoCloseable {

  private final ExecutorService executorService = Executors.newCachedThreadPool();
  private final RawAdapter adapter;
  private final byte[] responseTopic;

  Server(RawAdapter adapter, byte[] responseTopic) {
    this.adapter = adapter;
    this.responseTopic = responseTopic;
  }

  static Server start(RawAdapter adapter, byte[] responseTopic) {
    Server server = new Server(adapter, responseTopic);
    server.start();
    return server;
  }

  void start() {
    executorService.execute(() -> {
      Thread.currentThread().setName("Benchmark Server Thread");
      final Inbox inbox = adapter.dataClient().dataInbox();
      final Publisher publisher = adapter.dataClient().publisher();
      while (!Thread.interrupted()) {
        try {
          final byte[][] message = inbox.take();
          executorService.execute(() -> {
            try {
              final BenchmarkMessage request = DataProtocol.PAYLOAD_FRAME.get(message, BenchmarkMessage.parser());
              final BenchmarkMessage response = BenchmarkMessage.newBuilder(request).setServerTimestamp(System.nanoTime()).build();
              publisher.publish(responseTopic, response.toByteArray());
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
