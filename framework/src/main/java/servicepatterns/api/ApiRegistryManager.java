package servicepatterns.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.adapter.control.registry.RegistryApi;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.framework.descriptor.BaseTags;
import de.unistuttgart.isw.sfsc.framework.descriptor.ServiceDescriptor;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import servicepatterns.api.filtering.Filters;

final class ApiRegistryManager implements NotThrowingAutoCloseable {

  private final Set<Map<String, ByteString>> services = ConcurrentHashMap.newKeySet();
  private final StoreEventStreamConverter storeEventStreamConverter = new StoreEventStreamConverter(services);

  private final Handle handle;
  private final RegistryApi registryApi;

  ApiRegistryManager(RegistryApi registryApi) {
    this.registryApi = registryApi;
    handle = registryApi.addListener(storeEventStreamConverter);
  }

  Set<Map<String, ByteString>> getServices() {
    return Collections.unmodifiableSet(services);
  }

  Set<Map<String, ByteString>> getServices(String name) {
    return getServices()
        .stream()
        .filter(Filters.stringEqualsFilter(BaseTags.SFSC_SERVICE_NAME.name(), name))
        .collect(Collectors.toUnmodifiableSet());
  }

  Set<Map<String, ByteString>> getServices(String name, Message message, Collection<String> varPaths) {
    return getServices(name)
        .stream()
        .filter(Filters.regexFilter(message, varPaths))
        .collect(Collectors.toUnmodifiableSet());
  }

  Handle registerService(Map<String, ByteString> tags) {
    ByteString serviceDescriptor = ServiceDescriptor.newBuilder().putAllTags(tags).build().toByteString();
    registryApi.create(serviceDescriptor); //todo why returns future?
    return () -> registryApi.remove(serviceDescriptor);
  }

  Handle addServiceAddedListener(Consumer<Map<String, ByteString>> listener) {
    return storeEventStreamConverter.addServiceAddedListener(listener);
  }

  Handle addServiceRemovedListener(Consumer<Map<String, ByteString>> listener) {
    return storeEventStreamConverter.addServiceRemovedListener(listener);
  }

  public void close() {
    handle.close();
  }
}
