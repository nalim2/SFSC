package de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.commonjava.registry.CallbackRegistry;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.OutputPublisher;
import de.unistuttgart.isw.sfsc.commonjava.zmq.util.SubscriptionAgent;
import de.unistuttgart.isw.sfsc.framework.messagingpatterns.ackreqrep.RequestOrAcknowledge;
import de.unistuttgart.isw.sfsc.framework.messagingpatterns.ackreqrep.RequestOrAcknowledge.Request;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AckClient implements NotThrowingAutoCloseable {

  private final Supplier<Integer> idGenerator = new AtomicInteger()::getAndIncrement;
  private final CallbackRegistry callbackRegistry = new CallbackRegistry();

  private final ByteString replyTopic;
  private final OutputPublisher publisher;
  private final Handle handle;

  public AckClient(PubSubConnection pubSubConnection, ByteString replyTopic, Scheduler scheduler) {
    this.publisher = pubSubConnection.publisher();
    this.replyTopic = replyTopic;
    handle = SubscriptionAgent.create(pubSubConnection).addSubscriber(replyTopic, new AckClientConsumer(callbackRegistry, publisher), scheduler);
  }

  public void send(ByteString targetTopic, Message payload, Consumer<ByteString> consumer, int timoutMs, Runnable timeoutRunnable) {
    int id = idGenerator.get();
    RequestOrAcknowledge wrappedRequest = wrapRequest(id, payload);
    callbackRegistry.addCallback(id, consumer, timoutMs, timeoutRunnable);
    publisher.publish(targetTopic, wrappedRequest);
  }

  RequestOrAcknowledge wrapRequest(int id, Message payload) {
    Request request = Request.newBuilder()
        .setRequestPayload(payload.toByteString())
        .setReplyTopic(replyTopic)
        .setExpectedReplyId(id)
        .build();

    return RequestOrAcknowledge
        .newBuilder()
        .setRequest(request)
        .build();
  }

  @Override
  public void close() {
    handle.close();
    callbackRegistry.close();
  }
}
