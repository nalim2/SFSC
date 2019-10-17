package servicepatterns.basepatterns.simplereqrep;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.registry.CallbackRegistry;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import de.unistuttgart.isw.sfsc.patterns.simplereqrep.Request;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import servicepatterns.topiclistener.HandleFactory;
import servicepatterns.topiclistener.ListenerHandle;

public final class SimpleClient implements AutoCloseable {

  private final Supplier<Integer> idGenerator = new AtomicInteger()::getAndIncrement;
  private final CallbackRegistry callbackRegistry = new CallbackRegistry();

  private final ByteString replyTopic;
  private final OutputPublisher publisher;
  private final ListenerHandle listenerHandle;

  public SimpleClient(OutputPublisher publisher, ByteString replyTopic, HandleFactory handleFactory, Executor executor) {
    this.publisher = publisher;
    this.replyTopic = replyTopic;
    listenerHandle = handleFactory.attach(replyTopic, new SimpleClientConsumer(callbackRegistry), executor);
  }

  public void send(ByteString targetTopic, ByteString payload, Consumer<ByteString> consumer, int timeoutMs, Runnable timeoutRunnable) {
    int id = idGenerator.get();
    ByteString wrappedRequest = wrapRequest(id, payload);
    callbackRegistry.addCallback(id, consumer, timeoutMs, timeoutRunnable);
    publisher.publish(targetTopic, wrappedRequest);
  }

  ByteString wrapRequest(int id, ByteString payload) {
    return Request.newBuilder()
        .setReplyTopic(replyTopic)
        .setExpectedReplyId(id)
        .setRequestPayload(payload)
        .build()
        .toByteString();
  }

  @Override
  public void close() {
    listenerHandle.close();
    callbackRegistry.close();
  }

}
