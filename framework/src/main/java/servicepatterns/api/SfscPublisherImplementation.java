package servicepatterns.api;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Publisher;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

final class SfscPublisherImplementation implements SfscPublisher {

  private final ByteString topic;
  private final Publisher publisher;
  private final Executor executor;
  private final Map<String, ByteString> tags;
  private final Runnable onClose;

  SfscPublisherImplementation(Map<String, ByteString> tags, ByteString topic, Publisher publisher, Executor executor, Runnable onClose) {
    this.tags = Collections.unmodifiableMap(tags);
    this.topic = topic;
    this.publisher = publisher;
    this.executor = executor;
    this.onClose = onClose;
  }

  @Override
  public Map<String, ByteString> getTags() {
    return tags;
  }

  @Override
  public Future<Void> subscriptionFuture() {
    return publisher.addSubscriptionListener(topic);
  }

  @Override
  public Future<Void> subscriptionFuture(Runnable runnable) {
    return publisher.addSubscriptionListener(topic, () -> executor.execute(runnable));
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
