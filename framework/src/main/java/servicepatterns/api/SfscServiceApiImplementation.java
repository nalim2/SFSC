package servicepatterns.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.synchronizing.Awaitable;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servicepatterns.api.registry.ApiRegistryManager;
import servicepatterns.api.tagging.ServiceFactory;
import servicepatterns.basepatterns.ackreqrep.AckServerResult;

final class SfscServiceApiImplementation implements SfscServiceApi {

  private static final Logger logger = LoggerFactory.getLogger(SfscServiceApiImplementation.class);
  private final ExecutorService executorService = Executors.unconfigurableExecutorService(
      Executors.newCachedThreadPool(new ExceptionLoggingThreadFactory(getClass().getName(), logger))); //todo remove?

  private final ApiRegistryManager apiRegistryManager;
  private final ServiceFactory serviceFactory;

  SfscServiceApiImplementation(Adapter adapter) {
    apiRegistryManager = new ApiRegistryManager(adapter.registryClient());
    serviceFactory = new ServiceFactory(adapter.dataConnection(), apiRegistryManager, adapter.adapterInformation().getCoreId(),
        adapter.adapterInformation().getAdapterId());
  }

  @Override
  public Set<SfscServiceDescriptor> getServices() {
    return apiRegistryManager.getServices();
  }

  @Override
  public Set<SfscServiceDescriptor> getServices(String name) {
    return apiRegistryManager.getServices(name);
  }

  @Override
  public Set<SfscServiceDescriptor> getServices(String name, Message message, Collection<String> varPaths) {
    return apiRegistryManager.getServices(name, message, varPaths);
  }

  @Override
  public SfscServer server(SfscServerParameter parameter, Function<ByteString, AckServerResult> serverFunction) {
    return new SfscServerImplementation(parameter, serviceFactory, serverFunction);
  }

  @Override
  public SfscClient client() {
    return new SfscClientImplementation(this, serviceFactory);
  }

  @Override
  public SfscPublisher publisher(SfscPublisherParameter sfscPublisherParameter) {
    return new SfscPublisherImplementation(sfscPublisherParameter, serviceFactory);
  }

  @Override
  public SfscSubscriber subscriber(SfscServiceDescriptor publisherDescriptor, Consumer<ByteString> subscriberConsumer) {
    return new SfscSubscriberImplementation(publisherDescriptor, serviceFactory, subscriberConsumer);
  }

  @Override
  public SfscServer channelFactory(SfscChannelFactoryParameter parameter, Function<ByteString, SfscPublisher> channelFactory) {
    return new SfscChannelFactoryImplementation(parameter, serviceFactory, channelFactory);
  }

  @Override
  public Handle addRegistryStoreEventListener(Consumer<StoreEvent<SfscServiceDescriptor>> listener) {
    return apiRegistryManager.addStoreEventListener(storeEvent -> executorService.execute(() -> listener.accept(storeEvent)));
  }

  @Override
  public Handle addOneShotRegistryStoreEventListener(Predicate<StoreEvent<SfscServiceDescriptor>> predicate, Runnable runnable) {
    return apiRegistryManager.addOneShotStoreEventListener(predicate, runnable);
  }

  @Override
  public Awaitable addOneShotRegistryStoreEventListener(Predicate<StoreEvent<SfscServiceDescriptor>> predicate) {
    return apiRegistryManager.addOneShotStoreEventListener(predicate);
  }

  //todo close
}
