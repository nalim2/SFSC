package servicepatterns.basepatterns.ackreqrep;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import servicepatterns.CallbackRegistry;
import servicepatterns.topiclistener.HandleFactory;
import servicepatterns.topiclistener.ListenerHandle;

public final class AckServer implements AutoCloseable {

  private final CallbackRegistry callbackRegistry = new CallbackRegistry();
  private final ListenerHandle listenerHandle;

  public AckServer(OutputPublisher publisher, ScheduledExecutorService scheduledExecutorService, Function<ByteString, AckServerResult> serverFunction,
      ByteString serverTopic, int timeoutMs, int sendRateMs, int sendMaxTries, HandleFactory handleFactory, Executor executor) {
    listenerHandle = handleFactory.attach(
        serverTopic,
        new AckServerConsumer(publisher, callbackRegistry, scheduledExecutorService, serverFunction, serverTopic, timeoutMs, sendRateMs, sendMaxTries),
        executor);
  }

  @Override
  public void close() {
    listenerHandle.close();
    callbackRegistry.close();
  }
}

