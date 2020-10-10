package de.unistuttgart.isw.sfsc.adapter.control.registry;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.query.QueryRequest;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleClient;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class QueryClient implements NotThrowingAutoCloseable {

  private final ByteString serverTopic;
  private final SimpleClient simpleClient;
  private final int timeoutMs;

  QueryClient(PubSubConnection pubSubConnection, ByteString serverTopic, ByteString clientTopic, int timeoutMs, Scheduler scheduler) {
    simpleClient = new SimpleClient(pubSubConnection, clientTopic, scheduler);
    this.serverTopic = serverTopic;
    this.timeoutMs = timeoutMs;
  }

  void query(Supplier<Long> idSupplier, Consumer<ByteString> consumer, Runnable timeoutRunnable) {
    query(idSupplier.get(), consumer, timeoutRunnable);
  }

  void query(long id, Consumer<ByteString> consumer, Runnable timeoutRunnable) {
    ByteString command = QueryRequest.newBuilder().setEventId(id).build().toByteString();
    simpleClient.send(serverTopic, command, consumer, timeoutMs, timeoutRunnable);
  }

  @Override
  public void close() {
    simpleClient.close();
  }
}
