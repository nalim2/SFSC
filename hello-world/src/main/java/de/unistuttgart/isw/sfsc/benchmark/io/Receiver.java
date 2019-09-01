package de.unistuttgart.isw.sfsc.benchmark.io;

import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.benchmark.BenchmarkMessage;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol;
import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Inbox;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Receiver implements AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(Receiver.class);
  private final ExecutorService executorService = Executors.newCachedThreadPool(new ExceptionLoggingThreadFactory("BenchmarkReceiver", logger));
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
      while (!Thread.interrupted()) {
        try {
          final byte[][] message = inbox.take();
          executorService.execute(() -> {
            try {
              final long receiveTime = System.nanoTime();
              final BenchmarkMessage response = DataProtocol.PAYLOAD_FRAME.get(message, BenchmarkMessage.parser());
              final BenchmarkMessage finalResponse = BenchmarkMessage.newBuilder(response).setReceiveTimestamp(receiveTime).build();
              consumer.accept(finalResponse);
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
