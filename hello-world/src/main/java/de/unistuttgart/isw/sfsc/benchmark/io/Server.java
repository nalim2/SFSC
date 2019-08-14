package de.unistuttgart.isw.sfsc.benchmark.io;

import de.unistuttgart.isw.sfsc.benchmark.BenchmarkMessage;
import de.unistuttgart.isw.sfsc.client.adapter.Adapter;
import de.unistuttgart.isw.sfsc.util.Util;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import protocol.pubsub.DataProtocol;
import zmq.reactor.ReactiveSocket.Inbox;
import zmq.reactor.ReactiveSocket.Outbox;

class Server implements AutoCloseable {

  private final ExecutorService executorService = Executors.newCachedThreadPool();
  private final Adapter adapter;
  private final byte[] responseTopic;

  Server(Adapter adapter, byte[] responseTopic) {
    this.adapter = adapter;
    this.responseTopic = responseTopic;
  }

  static Server start(Adapter adapter, byte[] responseTopic) {
    Server server = new Server(adapter, responseTopic);
    server.start();
    return server;
  }

  void start() {
    executorService.execute(() -> {
      final Inbox inbox = adapter.getDataClient().getDataInbox();
      final Outbox outbox = adapter.getDataClient().getDataOutbox();
      while (!Thread.interrupted()) {
        try {
          final byte[][] message = inbox.take();
          executorService.execute(() -> {
            try {
              final BenchmarkMessage request = DataProtocol.PAYLOAD_FRAME.get(message, BenchmarkMessage.parser());
              final BenchmarkMessage response = BenchmarkMessage.newBuilder(request).setServerTimestamp(System.nanoTime()).build();
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
  }

  @Override
  public void close() {
    executorService.shutdownNow();
  }
}
