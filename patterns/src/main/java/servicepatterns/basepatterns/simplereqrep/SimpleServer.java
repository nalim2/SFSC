package servicepatterns.basepatterns.simplereqrep;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import java.util.concurrent.Executor;
import java.util.function.Function;
import servicepatterns.topiclistener.HandleFactory;
import servicepatterns.topiclistener.ListenerHandle;

public final class SimpleServer implements AutoCloseable {

  private final ListenerHandle listenerHandle;

  public SimpleServer(OutputPublisher publisher, HandleFactory handleFactory, Function<ByteString, ByteString> serverFunction, ByteString serverTopic,
      Executor executor) {
    listenerHandle = handleFactory.attach(serverTopic, new SimpleServerConsumer(publisher, serverFunction), executor);
  }

  @Override
  public void close() {
    listenerHandle.close();
  }
}

