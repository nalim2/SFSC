package servicepatterns.api;

import com.google.protobuf.ByteString;
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
    return subscriptionTracker.addSubscriptionListener(topic::equals, () -> null);
  }

  @Override
  public Handle onSubscription(Runnable runnable) {
    Future<Void> future = subscriptionTracker.addSubscriptionListener(topic::equals, () -> executor.execute(runnable), null);
    return () -> future.cancel(true);
  }

  @Override
  public void publish(ByteString payload) {
    publisher.send(topic, payload);
  }

  @Override
  public void close() {
    publisher.close();
    onClose.run();
  }
}
