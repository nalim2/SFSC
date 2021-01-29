package de.unistuttgart.isw.sfsc.framework.api.registry;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.adapter.control.RegistryApi;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.synchronizing.Awaitable;
import de.unistuttgart.isw.sfsc.commonjava.util.synchronizing.Synchronizer;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ApiRegistryManager implements NotThrowingAutoCloseable {

  private final Set<SfscServiceDescriptor> services = ConcurrentHashMap.newKeySet();
  private final StoreEventStreamConverter storeEventStreamConverter = new StoreEventStreamConverter(services);

  private final Handle handle;
  private final RegistryApi registryApi;

  public ApiRegistryManager(RegistryApi registryApi) {
    this.registryApi = registryApi;
    handle = registryApi.addListener(storeEventStreamConverter);
  }

  public Set<SfscServiceDescriptor> getServices() {
    return Collections.unmodifiableSet(services);
  }

  public Set<SfscServiceDescriptor> getServices(String name) {
    return getServices()
        .stream()
        .filter(descriptor -> Objects.equals(descriptor.getServiceName(), name))
        .collect(Collectors.toUnmodifiableSet());
  }

  public Set<SfscServiceDescriptor> getServices(String name, Message message, Collection<String> varPaths) {
    return getServices(name)
        .stream()
        .filter(getVarPathPredicate(message, varPaths))
        .collect(Collectors.toUnmodifiableSet());
  }

  Predicate<SfscServiceDescriptor> getVarPathPredicate(Message message, Collection<String> varPaths) {
    return varPaths
        .stream()
        .map(varPath -> new RegexFilter(message, varPath))
        .map(filter -> (Predicate<SfscServiceDescriptor>) filter)
        .reduce(Predicate::and)
        .orElseThrow();
  }

  public Handle registerService(SfscServiceDescriptor descriptor) {
    registryApi.create(descriptor); //todo why returns future?
    return () -> registryApi.remove(descriptor);
  }

  public Handle addStoreEventListener(Consumer<StoreEvent<SfscServiceDescriptor>> listener) {
    return storeEventStreamConverter.addListener(listener);
  }

  public Handle addOneShotStoreEventListener(Predicate<StoreEvent<SfscServiceDescriptor>> predicate, Runnable runnable) {
    return storeEventStreamConverter.addOneShotListener(predicate, runnable);
  }

  public Awaitable addOneShotStoreEventListener(Predicate<StoreEvent<SfscServiceDescriptor>> predicate) {
    Synchronizer synchronizer = new Synchronizer();
    storeEventStreamConverter.addOneShotListener(predicate, synchronizer);
    return synchronizer.getAwaitable();
  }

  public void close() {
    handle.close();
  }
}
