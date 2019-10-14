package servicepatterns.services.channelfactory;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.zmq.subscriptiontracker.SubscriptionTracker;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

class SubscriptionConfirmer implements Consumer<ByteString> {

  private final ByteString topic;
  private final Runnable runnable;
  private final SubscriptionTracker subscriptionTracker;
  private final Executor executor;

  SubscriptionConfirmer(ByteString topic, Runnable runnable, SubscriptionTracker subscriptionTracker, Executor executor) {
    this.topic = topic;
    this.runnable = runnable;
    this.subscriptionTracker = subscriptionTracker;
    this.executor = executor;
  }

  static SubscriptionConfirmer create(ByteString topic, Runnable runnable, SubscriptionTracker subscriptionTracker, Executor executor) {
    SubscriptionConfirmer subscriptionConfirmer = new SubscriptionConfirmer(topic, runnable, subscriptionTracker, executor);
    subscriptionTracker.addSubscriptionListener(subscriptionConfirmer);
    return subscriptionConfirmer;
  }

  @Override
  public void accept(ByteString topic) {
    if (this.topic.equals(topic)) {
      executor.execute(() -> {
            subscriptionTracker.removeSubscriptionListener(this);
            runnable.run();
          }
      );
    }
  }
}
