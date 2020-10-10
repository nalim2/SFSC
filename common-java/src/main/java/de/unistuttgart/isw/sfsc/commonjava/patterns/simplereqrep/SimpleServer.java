package de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.util.SubscriptionAgent;
import java.util.function.Function;

public final class SimpleServer implements NotThrowingAutoCloseable {

  private final Handle handle;

  public SimpleServer(PubSubConnection pubSubConnection, Function<ByteString, ByteString> serverFunction, ByteString serverTopic,
      Scheduler scheduler) {
    handle = SubscriptionAgent.create(pubSubConnection)
        .addSubscriber(serverTopic, new SimpleServerConsumer(pubSubConnection.publisher(), serverFunction), scheduler);
  }

  @Override
  public void close() {
    handle.close();
  }
}

