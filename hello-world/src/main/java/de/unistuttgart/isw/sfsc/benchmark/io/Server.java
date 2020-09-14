package de.unistuttgart.isw.sfsc.benchmark.io;


import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.benchmark.BenchmarkMessage;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleServer;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.SchedulerService;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;

class Server implements NotThrowingAutoCloseable {

  private final SchedulerService schedulerService;
  private final SimpleServer server;

  Server(PubSubConnection pubSubConnection, ByteString topic, int threadNumber) {
    schedulerService = new SchedulerService(threadNumber);
    server = new SimpleServer(pubSubConnection, Server::serverFunction, topic, schedulerService);
  }

  static ByteString serverFunction(ByteString bytes) {
    try {
      return BenchmarkMessage
          .newBuilder(BenchmarkMessage.parseFrom(bytes))
          .setServerTimestamp(System.nanoTime())
          .build()
          .toByteString();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
      return ByteString.EMPTY;
    }
  }

  @Override
  public void close() {
    server.close();
    schedulerService.close();
  }
}
