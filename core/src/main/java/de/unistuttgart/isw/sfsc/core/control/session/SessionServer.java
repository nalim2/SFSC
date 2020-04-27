package de.unistuttgart.isw.sfsc.core.control.session;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Hello;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Welcome;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleServer;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SessionServer implements NotThrowingAutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(SessionServer.class);

  private final SimpleServer simpleServer;

  SessionServer(SessionParameter parameter, PubSubConnection pubSubConnection, Executor executor,
      Listeners<Consumer<NewSessionEvent>> sessionListeners) {
    simpleServer = new SimpleServer(pubSubConnection, new SessionConsumer(sessionListeners, parameter), parameter.getSessionTopic(), executor);
  }

  @Override
  public void close() {
    simpleServer.close();
  }

  static class SessionConsumer implements Function<ByteString, ByteString> {

    private final Listeners<Consumer<NewSessionEvent>> sessionListeners;
    private final SessionParameter parameter;

    public SessionConsumer(Listeners<Consumer<NewSessionEvent>> sessionListeners, SessionParameter parameter) {
      this.sessionListeners = sessionListeners;
      this.parameter = parameter;
    }

    @Override
    public ByteString apply(ByteString byteString) {
      try {
        Hello hello = Hello.parseFrom(byteString);
        String adapterId = hello.getAdapterId();
        ByteString adapterHeartbeatTopic = hello.getHeartbeatTopic();
        logger.info("new session request from {}", hello.getAdapterId());
        sessionListeners.forEach(consumer -> consumer.accept(new NewSessionEvent(adapterId, adapterHeartbeatTopic)));
        return Welcome.newBuilder()
            .setDataPubPort(parameter.getDataPubPort())
            .setDataSubPort(parameter.getDataSubPort())
            .setCoreId(parameter.getCoreId())
            .build()
            .toByteString();
      } catch (InvalidProtocolBufferException e) {
        logger.warn("received malformed message", e);
        return ByteString.EMPTY;
      }
    }
  }

}
