package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent.StoreEventType;
import de.unistuttgart.isw.sfsc.commonjava.util.synchronizing.Awaitable;
import de.unistuttgart.isw.sfsc.commonjava.util.synchronizing.Synchronizer;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface SubscriptionTracker {

  Set<ByteString> getSubscriptions();

  Handle addListener(Consumer<StoreEvent<ByteString>> listener); //todo executor?

  Handle addOneShotListener(Predicate<StoreEvent<ByteString>> predicate, Runnable runnable);

  default Handle addOneShotSubscriptionListener(ByteString topic, Runnable runnable) {
    return addOneShotListener(storeEvent -> topic.equals(storeEvent.getData()) && storeEvent.getStoreEventType() == StoreEventType.CREATE, runnable);
  }

  default Handle addOneShotUnsubscriptionListener(ByteString topic, Runnable runnable) {
    return addOneShotListener(storeEvent -> topic.equals(storeEvent.getData()) && storeEvent.getStoreEventType() == StoreEventType.DELETE, runnable);
  }

  default Awaitable addOneShotSubscriptionListener(ByteString topic) {
    Synchronizer synchronizer = new Synchronizer();
    addOneShotSubscriptionListener(topic, synchronizer);
    return synchronizer.getAwaitable();
  }

  default Awaitable addOneShotUnsubscriptionListener(ByteString topic) {
    Synchronizer synchronizer = new Synchronizer();
    addOneShotUnsubscriptionListener(topic, synchronizer);
    return synchronizer.getAwaitable();
  }
}
