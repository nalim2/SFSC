package servicepatterns.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Publisher;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription.SubscriptionTracker;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

final class SfscPublisherImplementation implements SfscPublisher {

  private final ByteString topic;
  private final Publisher publisher;
  private final SubscriptionTracker subscriptionTracker;
  private final Executor executor;
  private final Map<String, ByteString> tags;
  private final Runnable onClose;

  SfscPublisherImplementation(Map<String, ByteString> tags, ByteString topic, Publisher publisher, SubscriptionTracker subscriptionTracker,
      Executor executor, Runnable onClose) {
    this.tags = Collections.unmodifiableMap(tags);
    this.topic = topic;
    this.publisher = publisher;
    this.subscriptionTracker = subscriptionTracker;
    this.executor = executor;
    this.onClose = onClose;
  }

  @Override
  public Map<String, ByteString> getTags() {
    return tags;
  }

  @Override
  public Future<Void> subscriptionFuture() {
    return subscriptionTracker.addOneShotSubscriptionListener(topic, () -> {});
  }

  @Override
  public Handle onSubscription(Runnable runnable) {
    Future<Void> future = subscriptionTracker.addOneShotSubscriptionListener(topic, () -> executor.execute(runnable));
    return () -> future.cancel(true);
  }

  @Override
  public Future<Void> unsubscriptionFuture() {
    return subscriptionTracker.addOneShotUnsubscriptionListener(topic, () -> {});
  }

  @Override
  public Handle onUnsubscription(Runnable runnable) {
    Future<Void> future = subscriptionTracker.addOneShotUnsubscriptionListener(topic, () -> executor.execute(runnable));
    return () -> future.cancel(true);
  }

  @Override
  public void publish(Message payload) {
    publisher.publish(topic, payload);
  }

  @Override
  public void close() {
    onClose.run();
  }
}
