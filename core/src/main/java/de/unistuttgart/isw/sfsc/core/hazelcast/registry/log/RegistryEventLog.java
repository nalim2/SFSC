package de.unistuttgart.isw.sfsc.core.hazelcast.registry.log;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.query.QueryReply;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.query.QueryReply.Expired;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.query.QueryReply.Future;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.query.QueryRequest;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.SchedulerService;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RegistryEventLog implements NotThrowingAutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(RegistryEventLog.class);
  private static final int THREAD_NUMBER = 1;

  private final Supplier<Long> idCounter = new AtomicLong()::getAndIncrement;
  private final AtomicLong logCounter = new AtomicLong();
  private final Map<Long, QueryReply> eventLog = new ConcurrentHashMap<>();
  private final NavigableMap<Long, QueryReply> staging = new ConcurrentSkipListMap<>();
  private final SchedulerService schedulerService = new SchedulerService(THREAD_NUMBER);
  private final Listeners<Consumer<QueryReply>> listeners = new Listeners<>();

  private final int removalRetentionTimeSec;

  public RegistryEventLog(int removeEventRetentionTimeSec) {
    this.removalRetentionTimeSec = removeEventRetentionTimeSec;
  }

  public Handle addListener(Consumer<QueryReply> listener) {
    return listeners.add(listener);
  }

  public void onStoreEvent(StoreEvent<SfscServiceDescriptor> storeEvent) {
    long id = idCounter.get();
    schedulerService.execute(() -> {
          switch (storeEvent.getStoreEventType()) {
            case CREATE: {
              onAdd(id, storeEvent.getData());
              break;
            }
            case DELETE: {
              onRemove(id, storeEvent.getData());
              break;
            }
            default: {
              logger.warn("Unsupported Store Event!");
            }
          }
        }
    );
  }

  void onAdd(long id, SfscServiceDescriptor byteString) {
      QueryReply queryReply = QueryReply.newBuilder().setCreated(byteString).setEventId(id).build();
      staging.put(id, queryReply);
      processStaging();
  }

  void onRemove(long id, SfscServiceDescriptor byteString) {
      QueryReply queryReply = QueryReply.newBuilder().setDeleted(byteString).setEventId(id).build();
      staging.put(id, queryReply);
      processStaging();
      discardCreateEvent(byteString);
      schedulerService.schedule(() -> eventLog.remove(id), removalRetentionTimeSec, TimeUnit.SECONDS);
  }

  void processStaging() {
    Map.Entry<Long, QueryReply> firstStagingEntry;
    while ((firstStagingEntry = staging.firstEntry()) != null) {
      Long key = firstStagingEntry.getKey();
      QueryReply value = firstStagingEntry.getValue();
      if (key == logCounter.get() && staging.remove(key, value)) {
        logger.info("adding log entry #{}", logCounter.get());
        eventLog.put(key, value);
        logCounter.getAndIncrement();
        listeners.forEach(listener -> listener.accept(value));
      } else {
        break;
      }
    }
  }

  void discardCreateEvent(SfscServiceDescriptor byteString) {
    eventLog.entrySet().stream()
        .filter(entry -> byteString.toByteString().equals(entry.getValue().getCreated().toByteString()))
        .map(Entry::getKey)
        .findAny()
        .ifPresentOrElse(eventLog::remove, () -> logger.warn("Could not discard, add event not found"));
  }

  public QueryReply handleQueryRequest(QueryRequest queryRequest) {
    long id = queryRequest.getEventId();
    long currentLogCount = logCounter.get();
    if (currentLogCount <= id) {
      return QueryReply.newBuilder().setEventId(id).setFuture(Future.newBuilder().setNewestValidEventId(currentLogCount)).build();
    } else {
      return Optional.ofNullable(eventLog.get(id))
          .orElseGet(() -> QueryReply.newBuilder().setEventId(id).setExpired(Expired.getDefaultInstance()).build());
    }
  }

  @Override
  public void close() {
    schedulerService.close();
  }
}
