package de.unistuttgart.isw.sfsc.core.hazelcast.registry.replicatedregistry;

import com.google.protobuf.ByteString;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent.StoreEventType;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.serverserver.registry.RegistryEntry;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

class EntryListenerAdapter implements EntryListener<RegistryEntry, Boolean> {

  private final ReentrantLock lock = new ReentrantLock(true);
  private final Consumer<StoreEvent<SfscServiceDescriptor>> registryEventHandler;

  EntryListenerAdapter(Consumer<StoreEvent<SfscServiceDescriptor>> registryEventHandler) {
    this.registryEventHandler = registryEventHandler;
  }

  @Override
  public void entryRemoved(EntryEvent<RegistryEntry, Boolean> event) {
    lock.lock();
    try {
      registryEventHandler.accept(new StoreEvent<>(StoreEventType.DELETE, event.getKey().getData()));
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void entryAdded(EntryEvent<RegistryEntry, Boolean> event) {
    lock.lock();
    try {
      registryEventHandler.accept(new StoreEvent<>(StoreEventType.CREATE, event.getKey().getData()));
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void entryEvicted(EntryEvent<RegistryEntry, Boolean> event) { }

  @Override
  public void entryUpdated(EntryEvent<RegistryEntry, Boolean> event) { }

  @Override
  public void mapCleared(MapEvent event) { }

  @Override
  public void mapEvicted(MapEvent event) { }

  @Override
  public void entryExpired(EntryEvent<RegistryEntry, Boolean> event) { }
}
