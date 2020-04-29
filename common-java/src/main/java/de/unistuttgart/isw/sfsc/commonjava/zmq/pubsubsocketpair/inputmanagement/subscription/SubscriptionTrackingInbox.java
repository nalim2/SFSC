package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription;


import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol.SubscriptionType;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.OneShotListener;
import de.unistuttgart.isw.sfsc.commonjava.util.QueueConnector;
import de.unistuttgart.isw.sfsc.commonjava.util.ReplayingListener;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent.StoreEventType;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Inbox;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionTrackingInbox implements SubscriptionTracker, NotThrowingAutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(SubscriptionTrackingInbox.class);
  private final Listeners<Consumer<StoreEvent<ByteString>>> listeners = new Listeners<>();
  private final Set<ByteString> subscriptions = new HashSet<>();
  private final QueueConnector<List<byte[]>> queueConnector;

  SubscriptionTrackingInbox(Inbox inbox) {
    queueConnector = new QueueConnector<>(inbox::take);
  }

  public static SubscriptionTrackingInbox create(Inbox inbox) {
    return new SubscriptionTrackingInbox(inbox);
  }

  public void start() {
    queueConnector.start(this::accept);
  }

  @Override
  public Set<ByteString> getSubscriptions() {
    return Collections.unmodifiableSet(subscriptions);
  }

  @Override
  public Handle addListener(Consumer<StoreEvent<ByteString>> listener) {
    ReplayingListener<ByteString> replayingListener = new ReplayingListener<>(listener);
    Handle handle = listeners.add(replayingListener);

    replayingListener.prepend(getSubscriptions());
    replayingListener.start();

    return handle;
  }

  @Override
  public Future<Void> addOneShotListener(Predicate<StoreEvent<ByteString>> predicate, Runnable runnable) {
    OneShotListener<StoreEvent<ByteString>> oneShotListener = new OneShotListener<>(predicate, runnable);
    Handle handle = listeners.add(oneShotListener);
    Future<Void> future = oneShotListener.initialize(handle);
    Set<StoreEvent<ByteString>> prepopulation = StoreEvent.toStoreEventSet(getSubscriptions());
    prepopulation.forEach(oneShotListener);
    return future;
  }

  @Override
  public Future<Void> addOneShotSubscriptionListener(ByteString topic, Runnable runnable) {
    return addOneShotListener(storeEvent -> topic.equals(storeEvent.getData()) && storeEvent.getStoreEventType() == StoreEventType.CREATE, runnable);
  }

  @Override
  public Future<Void> addOneShotUnsubscriptionListener(ByteString topic, Runnable runnable) {
    return addOneShotListener(storeEvent -> topic.equals(storeEvent.getData()) && storeEvent.getStoreEventType() == StoreEventType.DELETE, runnable);
  }

  void accept(List<byte[]> subscriptionMessage) {  //not thread safe, but we have just one so its okay
    if (!SubProtocol.isValid(subscriptionMessage)) {
      logger.warn("Received invalid subscription message");
    } else {
      SubscriptionType subscriptionType = SubProtocol.getSubscriptionType(subscriptionMessage);
      ByteString topic = ByteString.copyFrom(SubProtocol.getTopic(subscriptionMessage));
      switch (subscriptionType) {
        case SUBSCRIPTION: {
          subscriptions.add(topic);
          StoreEvent<ByteString> storeEvent = new StoreEvent<>(StoreEventType.CREATE, topic);
          logger.debug("Received subscription on topic {}", topic.toStringUtf8());
          listeners.forEach(listener -> listener.accept(storeEvent));
          break;
        }
        case UNSUBSCRIPTION: {
          subscriptions.remove(topic);
          StoreEvent<ByteString> storeEvent = new StoreEvent<>(StoreEventType.DELETE, topic);
          logger.debug("Received unsubscription on topic {}", topic.toStringUtf8());
          listeners.forEach(listener -> listener.accept(storeEvent));
          break;
        }
        default: {
          logger.warn("Received unsupported message type {}", subscriptionType);
          break;
        }
      }
    }
  }

  @Override
  public void close() {
    queueConnector.close();
  }

}
