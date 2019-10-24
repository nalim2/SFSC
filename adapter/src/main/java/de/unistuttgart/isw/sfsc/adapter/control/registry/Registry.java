package de.unistuttgart.isw.sfsc.adapter.control.registry;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.query.QueryReply;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent.StoreEventType;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEventQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Registry {

  private static final Logger logger = LoggerFactory.getLogger(Registry.class);

  private final Listeners<Consumer<StoreEvent>> entryListeners = new Listeners<>();
  private final Listeners<Runnable> notificationListeners = new Listeners<>();

  private final AtomicLong idCounter = new AtomicLong();
  private final Set<ByteString> registry = ConcurrentHashMap.newKeySet();

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
            if (idCounter.compareAndSet(queryId, queryId + 1)) {
              modifyRegistry(queryReply);
              notificationListeners.forEach(Runnable::run);
            }
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
        ByteString data = queryReply.getCreated();
        registry.add(data);
        onStoreEvent(StoreEventType.CREATE, data);
        break;
      }
      case DELETED: {
        ByteString data = queryReply.getDeleted();
        registry.remove(data);
        onStoreEvent(StoreEventType.DELETE, data);
        break;
      }
      default: //nothing to modify, so do nothing
    }

  }

  void onStoreEvent(StoreEventType type, ByteString data) {
    StoreEvent storeEvent = new StoreEvent(type, data);
    entryListeners.forEach(consumer -> consumer.accept(storeEvent));
  }

  Handle addEntryListener(Consumer<StoreEvent> listener) {
    StoreEventQueue storeEventQueue = new StoreEventQueue(listener);
    Handle handle = entryListeners.add(storeEventQueue);

    storeEventQueue.prepopulate(createStoreEventSnapshot());
    storeEventQueue.start();

    return handle;
  }

  Handle addNotificationListener(Runnable listener) {
    return notificationListeners.add(listener);
  }

  Set<ByteString> getRegistry() {
    return registry;
  }

  long getId() {
    return idCounter.get();
  }

  Set<StoreEvent> createStoreEventSnapshot() {
    return Set.copyOf(registry).stream()
        .map(entry -> new StoreEvent(StoreEventType.CREATE, entry))
        .collect(Collectors.toUnmodifiableSet());
  }
}

