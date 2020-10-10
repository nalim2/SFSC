package de.unistuttgart.isw.sfsc.core.control.registry;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.query.QueryRequest;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleServer;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.core.hazelcast.registry.Registry;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class QueryServer implements NotThrowingAutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(QueryServer.class);
  private final SimpleServer simpleServer;

  QueryServer(PubSubConnection pubSubConnection,ByteString topic, Scheduler scheduler, Registry registry) {
    simpleServer = new SimpleServer(pubSubConnection, new QueryConsumer(registry), topic, scheduler);
  }

  @Override
  public void close() {
    simpleServer.close();
  }

  static class QueryConsumer implements Function<ByteString, ByteString> {

    private final Registry registry;

    QueryConsumer(Registry registry) {this.registry = registry;}

    @Override
    public ByteString apply(ByteString byteString) {
      try {
        QueryRequest queryRequest = QueryRequest.parseFrom(byteString);
        return registry.handleQuery(queryRequest).toByteString();
      } catch (InvalidProtocolBufferException e) {
        logger.warn("received malformed message", e);
        return ByteString.EMPTY;
      }
    }
  }

}
