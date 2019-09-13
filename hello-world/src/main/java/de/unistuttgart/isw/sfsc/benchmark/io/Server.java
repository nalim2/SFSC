package de.unistuttgart.isw.sfsc.benchmark.io;

import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.benchmark.BenchmarkMessage;
import de.unistuttgart.isw.sfsc.client.adapter.raw.RawAdapter;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol;
import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.Publisher;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Inbox;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Server implements AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(Server.class);
  private final ExecutorService executorService = Executors.newCachedThreadPool(new ExceptionLoggingThreadFactory("BenchmarkServer", logger));
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
      final Inbox inbox = adapter.dataConnection().dataInbox();
      final Publisher publisher = adapter.dataConnection().publisher();
      while (!Thread.interrupted()) {
        try {
          final byte[][] message = inbox.take();
          executorService.execute(() -> {
            try {
              final BenchmarkMessage request = DataProtocol.PAYLOAD_FRAME.get(message, BenchmarkMessage.parser());
              final BenchmarkMessage response = BenchmarkMessage.newBuilder(request).setServerTimestamp(System.nanoTime()).build();
              publisher.publish(responseTopic, response);
            } catch (InvalidProtocolBufferException e) {
              e.printStackTrace();
            }
          });
        } catch (InterruptedException | RejectedExecutionException e) {
          Thread.currentThread().interrupt();
        }
      }
    });
  }

  @Override
  public void close() {
    executorService.shutdownNow();
  }
}
