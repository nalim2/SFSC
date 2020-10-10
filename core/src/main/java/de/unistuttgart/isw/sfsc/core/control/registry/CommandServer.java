package de.unistuttgart.isw.sfsc.core.control.registry;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.command.CommandRequest;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleServer;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.core.hazelcast.registry.Registry;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CommandServer implements NotThrowingAutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(CommandServer.class);

  private final SimpleServer simpleServer;

  CommandServer(PubSubConnection pubSubConnection, ByteString topic, Scheduler scheduler, Registry registry) {
    simpleServer = new SimpleServer(pubSubConnection, new RegistryConsumer(registry), topic, scheduler);
  }

  @Override
  public void close() {
    simpleServer.close();
  }

  static class RegistryConsumer implements Function<ByteString, ByteString> {

    private final Registry registry;

    RegistryConsumer(Registry registry) {this.registry = registry;}

    @Override
    public ByteString apply(ByteString byteString) {
      try {
        CommandRequest commandRequest = CommandRequest.parseFrom(byteString);
        return registry.handleCommand(commandRequest).toByteString();
      } catch (InvalidProtocolBufferException e) {
        logger.warn("received malformed message", e);
        return ByteString.EMPTY;
      }
    }
  }

}
