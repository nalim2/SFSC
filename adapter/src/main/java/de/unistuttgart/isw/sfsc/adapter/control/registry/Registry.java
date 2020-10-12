package de.unistuttgart.isw.sfsc.adapter.control.registry;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.query.QueryReply;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.ReplayingListener;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent.StoreEventType;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Registry {

  private static final Logger logger = LoggerFactory.getLogger(Registry.class);

  private final Listeners<Consumer<StoreEvent<SfscServiceDescriptor>>> entryListeners = new Listeners<>();
  private final Listeners<Runnable> notificationListeners = new Listeners<>();

  private final AtomicLong idCounter = new AtomicLong();
  private final Set<SfscServiceDescriptor> registry = ConcurrentHashMap.newKeySet();

  private final Scheduler scheduler;

  Registry(Scheduler scheduler) {this.scheduler = scheduler;}

  void handleQueryReply(ByteString byteString) {
    try {
      QueryReply queryReply = QueryReply.parseFrom(byteString);
      long queryId = queryReply.getEventId();
      if (queryId == idCounter.get()) {
        logger.info("received log event for id {} with case {}", queryId, queryReply.getCreatedOrDeletedOrExpiredOrFutureCase()); //todo delete
        switch (queryReply.getCreatedOrDeletedOrExpiredOrFutureCase()) {
          case CREATED: //fallthrough
          case DELETED: //fallthrough
          case EXPIRED: {
            idCounter.compareAndSet(queryId, queryId + 1);
            modifyRegistry(queryReply);
            scheduler.execute(() -> notificationListeners.forEach(Runnable::run));
            break;
          }
          case FUTURE: {
            break;
          }
          default: {
            logger.warn("Received unsupported message type {}", queryReply.getCreatedOrDeletedOrExpiredOrFutureCase());
            break;
          }
        }
      }
    } catch (InvalidProtocolBufferException e) {
      logger.warn("Received malformed message", e);
    }

  }

  void modifyRegistry(QueryReply queryReply) {
    switch (queryReply.getCreatedOrDeletedOrExpiredOrFutureCase()) {
      case CREATED: {
        SfscServiceDescriptor data = queryReply.getCreated();
        registry.add(data);
        onStoreEvent(StoreEventType.CREATE, data);
        break;
      }
      case DELETED: {
        SfscServiceDescriptor data = queryReply.getDeleted();
        registry.remove(data);
        onStoreEvent(StoreEventType.DELETE, data);
        break;
      }
      default: //nothing to modify, so do nothing
    }

  }

  void onStoreEvent(StoreEventType type, SfscServiceDescriptor data) {
    StoreEvent<SfscServiceDescriptor> storeEvent = new StoreEvent<>(type, data);
    scheduler.execute(() -> entryListeners.forEach(consumer -> consumer.accept(storeEvent)));
  }

  Handle addEntryListener(Consumer<StoreEvent<SfscServiceDescriptor>> listener) {
    ReplayingListener<SfscServiceDescriptor> replayingListener = new ReplayingListener<>(listener);
    Handle handle = entryListeners.add(replayingListener);

    replayingListener.prepend(getRegistry());
    replayingListener.start();

    return handle;
  }

  Handle addNotificationListener(Runnable listener) {
    return notificationListeners.add(listener);
  }

  Set<SfscServiceDescriptor> getRegistry() {
    return Collections.unmodifiableSet(registry);
  }

  long getId() {
    return idCounter.get();
  }
}

