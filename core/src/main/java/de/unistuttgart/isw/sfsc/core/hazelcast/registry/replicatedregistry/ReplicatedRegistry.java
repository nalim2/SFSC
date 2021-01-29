package de.unistuttgart.isw.sfsc.core.hazelcast.registry.replicatedregistry;

import com.google.protobuf.ByteString;
import com.hazelcast.replicatedmap.ReplicatedMap;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.ReplayingListener;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.serverserver.registry.RegistryEntry;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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

  public Handle addListener(Consumer<StoreEvent<SfscServiceDescriptor>> listener) {
    ReplayingListener<SfscServiceDescriptor> replayingListener = new ReplayingListener<>(listener);
    UUID handle = replicatedMap.addEntryListener(new EntryListenerAdapter(replayingListener));

    replayingListener.prepend(createStoreEventSnapshot());
    replayingListener.start();

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

  Set<SfscServiceDescriptor> createStoreEventSnapshot() {
    return Set.copyOf(replicatedMap.keySet()).stream()
        .map(RegistryEntry::getData)
        .collect(Collectors.toUnmodifiableSet());
  }

}
