package de.unistuttgart.isw.sfsc.framework.api.registry;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.LateComer;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.OneShotRunnable;
import de.unistuttgart.isw.sfsc.commonjava.util.ReplayingListener;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent.StoreEventType;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StoreEventStreamConverter implements Consumer<StoreEvent<ByteString>> {

  private static final Logger logger = LoggerFactory.getLogger(StoreEventStreamConverter.class);

  private final Listeners<Consumer<StoreEvent<SfscServiceDescriptor>>> listeners = new Listeners<>();

  private final Set<SfscServiceDescriptor> services;

  StoreEventStreamConverter(Set<SfscServiceDescriptor> services) {this.services = services;}

  @Override
  public void accept(StoreEvent<ByteString> storeEvent) {
    try {
      SfscServiceDescriptor descriptor = SfscServiceDescriptor.parseFrom(storeEvent.getData());
      switch (storeEvent.getStoreEventType()) {
        case CREATE: {
          services.add(descriptor);
          StoreEvent<SfscServiceDescriptor> convertedStoreEvent = new StoreEvent<>(StoreEventType.CREATE, descriptor);
          listeners.forEach(consumer -> consumer.accept(convertedStoreEvent));
          break;
        }
        case DELETE: {
          services.remove(descriptor);
          StoreEvent<SfscServiceDescriptor> convertedStoreEvent = new StoreEvent<>(StoreEventType.DELETE, descriptor);
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

  Handle addListener(Consumer<StoreEvent<SfscServiceDescriptor>> listener) {
    ReplayingListener<SfscServiceDescriptor> replayingListener = new ReplayingListener<>(listener);
    Handle handle = listeners.add(replayingListener);

    replayingListener.prepend(Collections.unmodifiableSet(services));
    replayingListener.start();

    return handle;
  }

  Handle addOneShotListener(Predicate<StoreEvent<SfscServiceDescriptor>> predicate, Runnable runnable) {
    LateComer lateComer = new LateComer();
    Consumer<StoreEvent<SfscServiceDescriptor>> consumer = event -> {
      if (predicate.test(event)) {
        lateComer.run();
      }
    };
    Handle handle = listeners.add(consumer);
    lateComer.set(new OneShotRunnable(() -> {
      runnable.run();
      handle.close();
    }));
    Set<StoreEvent<SfscServiceDescriptor>> prepopulation = StoreEvent.toStoreEventSet(Collections.unmodifiableSet(services));
    prepopulation.forEach(consumer);
    return handle;
  }
}
