package de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.registry.CallbackRegistry;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.util.SubscriptionAgent;
import java.util.function.Function;

public final class AckServer implements NotThrowingAutoCloseable {

  private final CallbackRegistry callbackRegistry = new CallbackRegistry();
  private final Handle handle;

  public AckServer(PubSubConnection pubSubConnection, Scheduler scheduler,
      Function<ByteString, AckServerResult> serverFunction,
      ByteString serverTopic, int timeoutMs, int sendRateMs, int sendMaxTries) {
    handle = SubscriptionAgent.create(pubSubConnection).addSubscriber(
        serverTopic,
        new AckServerConsumer(pubSubConnection.publisher(), callbackRegistry, scheduler, serverFunction, serverTopic, timeoutMs,
            sendRateMs, sendMaxTries),
        scheduler);
  }

  @Override
  public void close() {
    handle.close();
    callbackRegistry.close();
  }
}

