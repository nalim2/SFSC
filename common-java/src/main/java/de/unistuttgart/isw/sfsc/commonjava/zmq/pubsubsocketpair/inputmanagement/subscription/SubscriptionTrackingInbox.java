package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol.TYPE_AND_TOPIC_FRAME;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol.SubscriptionType;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.QueueConnector;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Inbox;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionTrackingInbox implements SubscriptionTracker, NotThrowingAutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(SubscriptionTrackingInbox.class);
  private final Listeners<Consumer<ByteString>> subscriptionListeners = new Listeners<>();
  private final Listeners<Consumer<ByteString>> unsubscriptionListeners = new Listeners<>();
  private final SubscriptionOverview subscriptionOverview = new SubscriptionOverview();
  private final QueueConnector<byte[][]> queueConnector;

  SubscriptionTrackingInbox(Inbox inbox) {queueConnector = new QueueConnector<>(inbox::take);}

  public static SubscriptionTrackingInbox create(Inbox inbox) {
    SubscriptionTrackingInbox subscriptionTrackingInbox = new SubscriptionTrackingInbox(inbox);
    subscriptionTrackingInbox.initialize();
    return subscriptionTrackingInbox;
  }

  void initialize() {
    addSubscriptionListener(subscriptionOverview::onSubscription);
    addUnsubscriptionListener(subscriptionOverview::onUnsubscription);
  }

  public void start() {
    queueConnector.start(this::accept);
  }

  @Override
  public Handle addSubscriptionListener(Consumer<ByteString> onSubscription) {
    return subscriptionListeners.add(onSubscription);
  }

  @Override
  public Handle addUnsubscriptionListener(Consumer<ByteString> onUnsubscription) {
    return unsubscriptionListeners.add(onUnsubscription);
  }

  @Override
  public Set<ByteString> getSubscriptions() {
    return subscriptionOverview.getSubscriptions();
  }


  @Override
  public <V> Future<V> addSubscriptionListener(Predicate<ByteString> predicate, Callable<V> callable) {
    return SubscriptionEventListener.addSubscriptionListener(this, predicate, callable);
  }

  @Override
  public <V> Future<V> addSubscriptionListener(Predicate<ByteString> predicate, Runnable runnable, V result) {
    return SubscriptionEventListener.addSubscriptionListener(this, predicate, runnable, result);
  }

  @Override
  public <V> Future<V> addUnsubscriptionListener(Predicate<ByteString> predicate, Callable<V> callable) {
    return SubscriptionEventListener.addUnsubscriptionListener(this, predicate, callable);
  }

  @Override
  public <V> Future<V> addUnsubscriptionListener(Predicate<ByteString> predicate, Runnable runnable, V result) {
    return SubscriptionEventListener.addUnsubscriptionListener(this, predicate, runnable, result);
  }

  void accept(byte[][] subscriptionMessage) {
    byte[] typeAndTopicFrame = TYPE_AND_TOPIC_FRAME.get(subscriptionMessage);
    SubscriptionType subscriptionType = SubProtocol.getSubscriptionType(typeAndTopicFrame);
    switch (subscriptionType) {
      case SUBSCRIPTION: {
        ByteString topic = SubProtocol.getTopicMessage(typeAndTopicFrame);
        subscriptionListeners.forEach(listener -> listener.accept(topic));
        break;
      }
      case UNSUBSCRIPTION: {
        ByteString topic = SubProtocol.getTopicMessage(typeAndTopicFrame);
        unsubscriptionListeners.forEach(listener -> listener.accept(topic));
        break;
      }
      default: {
        logger.warn("Received unsupported message type {}", subscriptionType);
        break;
      }
    }
  }

  @Override
  public void close() {
    queueConnector.close();
  }

}
