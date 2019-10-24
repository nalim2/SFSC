package de.unistuttgart.isw.sfsc.benchmark.io;


import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.benchmark.BenchmarkMessage;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleServer;
import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Server implements NotThrowingAutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(Server.class);
  private final SimpleServer server;

  Server(PubSubConnection pubSubConnection, ByteString topic, int threadNumber) {
    Executor executor = Executors.newFixedThreadPool(threadNumber, new ExceptionLoggingThreadFactory(getClass().getName(), logger));
    server = new SimpleServer(pubSubConnection, Server::serverFunction, topic, executor);
  }

  static ByteString serverFunction(ByteString bytes) {
    try {
      return BenchmarkMessage.newBuilder(BenchmarkMessage.parseFrom(bytes)).setServerTimestamp(System.nanoTime()).build().toByteString();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
      return ByteString.EMPTY;
    }
  }

  @Override
  public void close() {
    server.close();
  }
}
