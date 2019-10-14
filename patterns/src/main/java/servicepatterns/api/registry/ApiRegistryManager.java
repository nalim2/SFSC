package servicepatterns.api.registry;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.adapter.base.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.patterns.tags.BaseTags;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import servicepatterns.api.filters.FilterFactory;

public final class ApiRegistryManager {

  private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
private final FilterFactory filterFactory = new FilterFactory();

  private final RegistryClient registryClient;
  private Set<Map<String, ByteString>> services;

  public ApiRegistryManager(RegistryClient registryClient) {
    this.registryClient = registryClient;
    scheduledExecutorService.scheduleAtFixedRate(() -> {
      try {
        services = Collections.unmodifiableSet(registryClient.getServices().get());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }, 100, 100, TimeUnit.MILLISECONDS);
  }

  public Set<Map<String, ByteString>> getServices() {
    return services;
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

  public Registration registerService(Map<String, ByteString> tags) {
    registryClient.addService(tags); //todo why returns future?
    return () -> registryClient.removeService(tags);
  }
}
