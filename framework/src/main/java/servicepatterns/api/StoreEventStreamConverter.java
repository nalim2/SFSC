package servicepatterns.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.OneShotListener;
import de.unistuttgart.isw.sfsc.commonjava.util.ReplayingListener;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent.StoreEventType;
import de.unistuttgart.isw.sfsc.framework.descriptor.ServiceDescriptor;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StoreEventStreamConverter implements Consumer<StoreEvent<ByteString>> {

  private static final Logger logger = LoggerFactory.getLogger(StoreEventStreamConverter.class);

  private final Listeners<Consumer<StoreEvent<Map<String, ByteString>>>> listeners = new Listeners<>();

  private final Set<Map<String, ByteString>> services;

  StoreEventStreamConverter(Set<Map<String, ByteString>> services) {this.services = services;}

  @Override
  public void accept(StoreEvent<ByteString> storeEvent) {
    try {
      Map<String, ByteString> service = ServiceDescriptor.parseFrom(storeEvent.getData()).getTagsMap();
      switch (storeEvent.getStoreEventType()) {
        case CREATE: {
          services.add(service);
          StoreEvent<Map<String, ByteString>> convertedStoreEvent = new StoreEvent<>(StoreEventType.CREATE, service);
          listeners.forEach(consumer -> consumer.accept(convertedStoreEvent));
          break;
        }
        case DELETE: {
          services.remove(service);
          StoreEvent<Map<String, ByteString>> convertedStoreEvent = new StoreEvent<>(StoreEventType.DELETE, service);
          listeners.forEach(consumer -> consumer.accept(convertedStoreEvent));
          break;
        }
        default: {
          logger.warn("Received unsupported store event with type {}", storeEvent.getStoreEventType());
          break;
        }
      }
    } catch (InvalidProtocolBufferException e) {
      logger.warn("Registry contains malformed entries", e);
    }
  }

  Handle addListener(Consumer<StoreEvent<Map<String, ByteString>>> listener) {
    ReplayingListener<Map<String, ByteString>> replayingListener = new ReplayingListener<>(listener);
    Handle handle = listeners.add(replayingListener);

    replayingListener.prepend(Collections.unmodifiableSet(services));
    replayingListener.start();

    return handle;
  }

  Future<Void> addOneShotListener(Predicate<StoreEvent<Map<String, ByteString>>> predicate, Runnable runnable) {
    OneShotListener<StoreEvent<Map<String, ByteString>>> oneShotListener = new OneShotListener<>(predicate, runnable);
    Handle handle = listeners.add(oneShotListener);
    Future<Void> future = oneShotListener.initialize(handle);
    Set<StoreEvent<Map<String, ByteString>>> prepopulation = StoreEvent.toStoreEventSet(Collections.unmodifiableSet(services));
    prepopulation.forEach(oneShotListener);
    return future;
  }
}
