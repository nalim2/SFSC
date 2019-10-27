package de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.registry.CallbackRegistry;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.OutputPublisher;
import de.unistuttgart.isw.sfsc.commonjava.zmq.util.SubscriptionAgent;
import de.unistuttgart.isw.sfsc.messagingpatterns.reqrep.Request;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SimpleClient implements NotThrowingAutoCloseable {

  private final Supplier<Integer> idGenerator = new AtomicInteger()::getAndIncrement;
  private final CallbackRegistry callbackRegistry = new CallbackRegistry();

  private final ByteString replyTopic;
  private final OutputPublisher publisher;
  private final Handle handle;

  public SimpleClient(PubSubConnection pubSubConnection, ByteString replyTopic, Executor executor) {
    this.publisher = pubSubConnection.publisher();
    this.replyTopic = replyTopic;
    handle = SubscriptionAgent.create(pubSubConnection).addSubscriber(replyTopic, new SimpleClientConsumer(callbackRegistry), executor);
  }

  public void send(ByteString targetTopic, ByteString payload, Consumer<ByteString> consumer, int timeoutMs, Runnable timeoutRunnable) {
    int id = idGenerator.get();
    Request wrappedRequest = wrapRequest(id, payload);
    callbackRegistry.addCallback(id, consumer, timeoutMs, timeoutRunnable);
    publisher.publish(targetTopic, wrappedRequest);
  }

  Request wrapRequest(int id, ByteString payload) {
    return Request.newBuilder()
        .setReplyTopic(replyTopic)
        .setExpectedReplyId(id)
        .setRequestPayload(payload)
        .build();
  }

  @Override
  public void close() {
    handle.close();
    callbackRegistry.close();
  }

}
