package de.unistuttgart.isw.sfsc.core.hazelcast.registry.replicatedregistry;

import com.hazelcast.core.ReplicatedMap;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent.StoreEventType;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEventQueue;
import de.unistuttgart.isw.sfsc.serverserver.registry.RegistryEntry;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicatedRegistry {

  private static final Logger logger = LoggerFactory.getLogger(ReplicatedRegistry.class);
  private final ReplicatedMap<RegistryEntry, Boolean> replicatedMap; //for some reason, ReplicatedMap does a nn check on value. Void -> Boolean

  public ReplicatedRegistry(ReplicatedMap<RegistryEntry, Boolean> replicatedMap) {
    this.replicatedMap = replicatedMap;
  }

  public Handle addListener(Consumer<StoreEvent> listener) {
    StoreEventQueue storeEventQueue = new StoreEventQueue(listener);
    String handle = replicatedMap.addEntryListener(new EntryListenerAdapter(storeEventQueue));

    storeEventQueue.prepopulate(createStoreEventSnapshot());
    storeEventQueue.start();

    return () -> replicatedMap.removeEntryListener(handle);
  }

  public void add(RegistryEntry registryEntry) {
    replicatedMap.put(registryEntry, Boolean.TRUE);
    logger.debug("Registry entry added");
  }

  public void remove(RegistryEntry registryEntry) {
    replicatedMap.remove(registryEntry, Boolean.TRUE);
    logger.debug("Registry entry removed");
  }

  public void removeAll(Predicate<RegistryEntry> predicate) {
    Set<RegistryEntry> copy = new HashSet<>(replicatedMap.keySet());
    copy.removeIf(predicate.negate());
    copy.forEach(replicatedMap::remove);
  }

  Set<StoreEvent> createStoreEventSnapshot() {
    return Set.copyOf(replicatedMap.keySet()).stream()
        .map(entry -> new StoreEvent(StoreEventType.CREATE, entry.getData()))
        .collect(Collectors.toUnmodifiableSet());
  }

}
