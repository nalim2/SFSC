package de.unistuttgart.isw.sfsc.core.control.session;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Hello;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Welcome;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleServer;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import java.util.concurrent.Executor;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SessionServer implements NotThrowingAutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(SessionServer.class);

  private final SimpleServer simpleServer;

  SessionServer(Configuration<CoreOption> configuration, PubSubConnection pubSubConnection, ByteString serverTopic, Executor executor,
      String coreId) {
    int dataPubPort = Integer.parseInt(configuration.get(CoreOption.DATA_PUB_PORT));
    int dataSubPort = Integer.parseInt(configuration.get(CoreOption.DATA_SUB_PORT));
    simpleServer = new SimpleServer(pubSubConnection, new SessionConsumer(dataPubPort, dataSubPort, coreId), serverTopic, executor);
  }

  @Override
  public void close() {
    simpleServer.close();
  }

  static class SessionConsumer implements Function<ByteString, ByteString> {

    private final int dataPubPort;
    private final int dataSubPort;
    private final String coreId;

    SessionConsumer(int dataPubPort, int dataSubPort, String coreId) {
      this.dataPubPort = dataPubPort;
      this.dataSubPort = dataSubPort;
      this.coreId = coreId;
    }


    @Override
    public ByteString apply(ByteString byteString) {
      try {
        Hello hello = Hello.parseFrom(byteString);
        logger.info("new session request from {}", hello.getAdapterId());
        return Welcome.newBuilder()
            .setDataPubPort(dataPubPort)
            .setDataSubPort(dataSubPort)
            .setCoreId(coreId)
            .build()
            .toByteString();
      } catch (InvalidProtocolBufferException e) {
        logger.warn("received malformed message", e);
        return ByteString.EMPTY;
      }
    }
  }

}
