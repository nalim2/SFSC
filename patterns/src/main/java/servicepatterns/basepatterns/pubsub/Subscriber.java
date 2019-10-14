package servicepatterns.basepatterns.pubsub;

import com.google.protobuf.ByteString;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import servicepatterns.topiclistener.HandleFactory;
import servicepatterns.topiclistener.ListenerHandle;

public final class Subscriber implements AutoCloseable {

  private final ListenerHandle listenerHandle;

  public Subscriber(Consumer<ByteString> subscriberFunction, ByteString topic, Executor executor, HandleFactory handleFactory) {
    listenerHandle = handleFactory.attach(topic, subscriberFunction, executor);
  }

  @Override
  public void close() {
    listenerHandle.close();
  }
}
