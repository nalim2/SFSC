package servicepatterns.basepatterns.ackreqrep;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.registry.CallbackRegistry;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import de.unistuttgart.isw.sfsc.patterns.ackreqrep.RequestOrAcknowledge;
import de.unistuttgart.isw.sfsc.patterns.ackreqrep.RequestOrAcknowledge.Request;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import servicepatterns.topiclistener.HandleFactory;
import servicepatterns.topiclistener.ListenerHandle;

public final class AckClient implements AutoCloseable {

  private final Supplier<Integer> idGenerator = new AtomicInteger()::getAndIncrement;
  private final CallbackRegistry callbackRegistry = new CallbackRegistry();

  private final ByteString replyTopic;
  private final OutputPublisher publisher;
  private final ListenerHandle listenerHandle;

  public AckClient(OutputPublisher publisher, ByteString replyTopic, HandleFactory handleFactory, Executor executor) {
    this.publisher = publisher;
    this.replyTopic = replyTopic;
    listenerHandle = handleFactory.attach(replyTopic, new AckClientConsumer(callbackRegistry, publisher), executor);
  }

  public void send(ByteString targetTopic, ByteString payload, Consumer<ByteString> consumer, int timoutMs, Runnable timeoutRunnable) {
    int id = idGenerator.get();
    ByteString wrappedRequest = wrapRequest(id, payload);
    callbackRegistry.addCallback(id, consumer, timoutMs, timeoutRunnable);
    publisher.publish(targetTopic, wrappedRequest);
  }

  ByteString wrapRequest(int id, ByteString payload) {
    Request request = Request.newBuilder()
        .setRequestPayload(payload)
        .setReplyTopic(replyTopic)
        .setExpectedReplyId(id)
        .build();

    return RequestOrAcknowledge
        .newBuilder()
        .setRequest(request)
        .build()
        .toByteString();
  }

  @Override
  public void close() {
    listenerHandle.close();
    callbackRegistry.close();
  }
}
