package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface SubscriptionTracker {

  Set<ByteString> getSubscriptions();

  Handle addListener(Consumer<StoreEvent<ByteString>> listener); //todo executor?

  Future<Void> addOneShotListener(Predicate<StoreEvent<ByteString>> predicate, Runnable runnable);

  Future<Void> addOneShotSubscriptionListener(ByteString topic, Runnable runnable);

}
