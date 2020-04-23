package de.unistuttgart.isw.sfsc.adapter.control.registry;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.command.CommandRequest;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleClient;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

final class CommandClient implements NotThrowingAutoCloseable {

  private final ByteString serverTopic;
  private final SimpleClient simpleClient;
  private final int timeoutMs;

  CommandClient(PubSubConnection pubSubConnection, ByteString serverTopic, ByteString clientTopic, int timeoutMs, Executor executor) {
    simpleClient = new SimpleClient(pubSubConnection, clientTopic, executor);
    this.serverTopic = serverTopic;
    this.timeoutMs = timeoutMs;
  }

  void create(ByteString entry, String adapterId, Consumer<ByteString> consumer, Runnable timeoutRunnable) {
    ByteString command = CommandRequest.newBuilder().setAdapterId(adapterId).setCreate(entry).build().toByteString();
    simpleClient.send(serverTopic, command, consumer, timeoutMs, timeoutRunnable);
  }

  void remove(ByteString entry, String adapterId, Consumer<ByteString> consumer, Runnable timeoutRunnable) {
    //todo dont consume bytestring but commandreply
    ByteString command = CommandRequest.newBuilder().setAdapterId(adapterId).setDelete(entry).build().toByteString();
    simpleClient.send(serverTopic, command, consumer, timeoutMs, timeoutRunnable);
  }

  @Override
  public void close() {
    simpleClient.close();
  }
}
