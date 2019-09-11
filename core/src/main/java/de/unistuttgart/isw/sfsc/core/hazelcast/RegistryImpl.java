package de.unistuttgart.isw.sfsc.core.hazelcast;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;
import com.hazelcast.core.ReplicatedMap;
import de.unistuttgart.isw.sfsc.commonjava.registry.SimpleRegistrySet;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor;
import java.util.Set;

class RegistryImpl implements Registry {

  private final SimpleRegistrySet<ServiceDescriptor> registry = SimpleRegistrySet.getInstance();
  private final ReplicatedMap<ServiceDescriptor, Boolean> replicationMap; //for some reason, ReplicatedMap does a nn check on value. Void -> Boolean

  RegistryImpl(ReplicatedMap<ServiceDescriptor, Boolean> replicationMap) {
    this.replicationMap = replicationMap;
    replicationMap.addEntryListener(new Listener(registry));
    registry.addAll(replicationMap.keySet());
  }

  public void create(ServiceDescriptor serviceDescriptor) {
    replicationMap.put(serviceDescriptor, Boolean.TRUE);
  }

  public Set<ServiceDescriptor> read() {
    return registry.getEntries();
  }

  public void delete(ServiceDescriptor serviceDescriptor) {
    replicationMap.remove(serviceDescriptor);
  }

  static class Listener implements EntryListener<ServiceDescriptor, Boolean> {

    private final SimpleRegistrySet<ServiceDescriptor> registry;

    Listener(SimpleRegistrySet<ServiceDescriptor> registry) {
      this.registry = registry;
    }

    @Override
    public void entryRemoved(EntryEvent<ServiceDescriptor, Boolean> event) {
      registry.remove(event.getKey());
    }

    @Override
    public void entryAdded(EntryEvent<ServiceDescriptor, Boolean> event) {
      registry.add(event.getKey());
    }

    @Override
    public void entryEvicted(EntryEvent<ServiceDescriptor, Boolean> event) {
    }

    @Override
    public void entryUpdated(EntryEvent<ServiceDescriptor, Boolean> event) {
    }

    @Override
    public void mapCleared(MapEvent event) {
    }

    @Override
    public void mapEvicted(MapEvent event) {
    }
  }

}
