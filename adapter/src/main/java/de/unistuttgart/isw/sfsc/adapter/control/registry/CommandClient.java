package de.unistuttgart.isw.sfsc.adapter.control.registry;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.command.CommandRequest;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleClient;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.types.SfscId;
import java.util.function.Consumer;

final class CommandClient implements NotThrowingAutoCloseable {

  private final ByteString serverTopic;
  private final SimpleClient simpleClient;
  private final int timeoutMs;

  CommandClient(PubSubConnection pubSubConnection, ByteString serverTopic, ByteString clientTopic, int timeoutMs, Scheduler scheduler) {
    simpleClient = new SimpleClient(pubSubConnection, clientTopic, scheduler);
    this.serverTopic = serverTopic;
    this.timeoutMs = timeoutMs;
  }

  void create(SfscServiceDescriptor entry, String adapterId, Consumer<ByteString> consumer, Runnable timeoutRunnable) {
    ByteString command = CommandRequest.newBuilder().setAdapterId(SfscId.newBuilder().setId(adapterId).build()).setCreateRequest(entry).build().toByteString();
    simpleClient.send(serverTopic, command, consumer, timeoutMs, timeoutRunnable);
  }

  void remove(SfscServiceDescriptor entry, String adapterId, Consumer<ByteString> consumer, Runnable timeoutRunnable) {
    //todo dont consume bytestring but commandreply
    ByteString command = CommandRequest.newBuilder().setAdapterId(SfscId.newBuilder().setId(adapterId).build()).setDeleteRequest(entry).build().toByteString();
    simpleClient.send(serverTopic, command, consumer, timeoutMs, timeoutRunnable);
  }

  @Override
  public void close() {
    simpleClient.close();
  }
}
