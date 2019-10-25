package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface SubscriptionTracker {

  Set<ByteString> getSubscriptions();

  Handle addListener(Consumer<StoreEvent<ByteString>> listener);

  <V> Future<V> addOneShotListener(Predicate<StoreEvent<ByteString>> predicate, Callable<V> callable);

  <V> Future<V> addOneShotSubscriptionListener(ByteString topic, Callable<V> callable);

}
