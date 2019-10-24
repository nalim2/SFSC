package servicepatterns.api.registry;

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
import java.util.stream.Collectors;
import servicepatterns.api.filters.FilterFactory;

public final class ApiRegistryManagerImplementation implements NotThrowingAutoCloseable {

  private final FilterFactory filterFactory = new FilterFactory();
  private final Set<Map<String, ByteString>> services = ConcurrentHashMap.newKeySet();

  private final Handle handle;
  private final RegistryApi registryApi;

  public ApiRegistryManagerImplementation(RegistryApi registryApi) {
    this.registryApi = registryApi;
    handle = registryApi.addListener(new StoreEventStreamConverter(services));
  }

  public Set<Map<String, ByteString>> getServices() {
    return Collections.unmodifiableSet(services);
  }

  public Set<Map<String, ByteString>> getServices(String name) {
    return getServices()
        .stream()
        .filter(filterFactory.stringEqualsFilter(BaseTags.SFSC_SERVICE_NAME.name(), name))
        .collect(Collectors.toUnmodifiableSet());
  }

  public Set<Map<String, ByteString>> getServices(String name, Message message, Collection<String> varPaths) {
    return getServices(name)
        .stream()
        .filter(filterFactory.regexFilter(message, varPaths))
        .collect(Collectors.toUnmodifiableSet());
  }

  public Handle registerService(Map<String, ByteString> tags) {
    ByteString serviceDescriptor = ServiceDescriptor.newBuilder().putAllTags(tags).build().toByteString();
    registryApi.create(serviceDescriptor); //todo why returns future?
    return () -> registryApi.remove(serviceDescriptor);
  }

  public void close() {
    handle.close();
  }
}
