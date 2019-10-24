package de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription.SubscriptionTracker;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.OutputPublisher;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public final class Publisher implements NotThrowingAutoCloseable {

  private final Set<Future<Void>> futures = ConcurrentHashMap.newKeySet();

  private final OutputPublisher publisher;
  private final SubscriptionTracker subscriptionTracker;

  public Publisher(PubSubConnection pubSubConnection) {
    this.publisher = pubSubConnection.publisher();
    this.subscriptionTracker = pubSubConnection.subscriptionTracker();
  }

  public void send(ByteString targetTopic, ByteString payload) {
    publisher.publish(targetTopic, payload);
  }

  public Future<Void> addSubscriptionListener(ByteString targetTopic) {
    return subscriptionTracker.addSubscriptionListener(targetTopic, () -> null);
  }

  public Future<Void> addSubscriptionListener(ByteString targetTopic, Runnable runnable) {
    return subscriptionTracker.addSubscriptionListener(targetTopic, runnable, null);
  }

  public Future<Void> addUnsubscriptionListener(ByteString targetTopic) {
    return subscriptionTracker.addUnsubscriptionListener(targetTopic, () -> null);
  }

  public Future<Void> addUnsubscriptionListener(ByteString targetTopic, Runnable runnable) {
    return subscriptionTracker.addUnsubscriptionListener(targetTopic, runnable, null);
  }

  @Override
  public void close() {
    futures.forEach(future -> future.cancel(true));
  }
}
