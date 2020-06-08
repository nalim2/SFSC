package de.unistuttgart.isw.sfsc.framework.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.synchronizing.Awaitable;
import de.unistuttgart.isw.sfsc.framework.api.registry.ApiRegistryManager;
import de.unistuttgart.isw.sfsc.framework.api.services.ServiceFactory;
import de.unistuttgart.isw.sfsc.framework.api.services.channelfactory.ChannelFactoryResult;
import de.unistuttgart.isw.sfsc.framework.api.services.channelfactory.ChannelFactoryServer;
import de.unistuttgart.isw.sfsc.framework.api.services.channelfactory.SfscChannelFactoryParameter;
import de.unistuttgart.isw.sfsc.framework.api.services.clientserver.SfscClient;
import de.unistuttgart.isw.sfsc.framework.api.services.clientserver.SfscClientImplementation;
import de.unistuttgart.isw.sfsc.framework.api.services.clientserver.SfscServer;
import de.unistuttgart.isw.sfsc.framework.api.services.clientserver.SfscServerImplementation;
import de.unistuttgart.isw.sfsc.framework.api.services.clientserver.SfscServerParameter;
import de.unistuttgart.isw.sfsc.framework.api.services.pubsub.SfscPublisher;
import de.unistuttgart.isw.sfsc.framework.api.services.pubsub.SfscPublisherImplementation;
import de.unistuttgart.isw.sfsc.framework.api.services.pubsub.SfscPublisherParameter;
import de.unistuttgart.isw.sfsc.framework.api.services.pubsub.SfscSubscriber;
import de.unistuttgart.isw.sfsc.framework.api.services.pubsub.SfscSubscriberImplementation;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep.AckServerResult;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SfscServiceApiImplementation implements SfscServiceApi {

  private static final Logger logger = LoggerFactory.getLogger(SfscServiceApiImplementation.class);
  private final ExecutorService executorService = Executors.unconfigurableExecutorService(Executors.newCachedThreadPool(
      new ExceptionLoggingThreadFactory(getClass().getName(), logger))); //todo remove?

  private final ApiRegistryManager apiRegistryManager;
  private final ServiceFactory serviceFactory;
  private final Adapter adapter;

  SfscServiceApiImplementation(Adapter adapter) {
    apiRegistryManager = new ApiRegistryManager(adapter.registryClient());
    serviceFactory = new ServiceFactory(
        adapter.dataConnection(),
        apiRegistryManager,
        adapter.adapterInformation().getCoreId(),
        adapter.adapterInformation().getAdapterId());
    this.adapter = adapter;
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
  public SfscServer channelFactory(SfscChannelFactoryParameter parameter,
      Function<ByteString, ChannelFactoryResult> channelFactory) {
    ChannelFactoryServer channelFactoryServer = new ChannelFactoryServer(channelFactory);
    SfscServerParameter sfscServerParameter = parameter.toSfscServerParameter();
    return new SfscServerImplementation(sfscServerParameter, serviceFactory, channelFactoryServer);
  }

  @Override
  public Handle addRegistryStoreEventListener(Consumer<StoreEvent<SfscServiceDescriptor>> listener) {
    return apiRegistryManager.addStoreEventListener(storeEvent -> executorService.execute(() -> listener.accept(
        storeEvent)));
  }

  @Override
  public Handle addOneShotRegistryStoreEventListener(Predicate<StoreEvent<SfscServiceDescriptor>> predicate,
      Runnable runnable) {
    return apiRegistryManager.addOneShotStoreEventListener(predicate, runnable);
  }

  @Override
  public Awaitable addOneShotRegistryStoreEventListener(Predicate<StoreEvent<SfscServiceDescriptor>> predicate) {
    return apiRegistryManager.addOneShotStoreEventListener(predicate);
  }

  @Override
  public Handle addCoreLostEventListener(Runnable runnable) {
    return adapter.addCoreLostEventListener(runnable);
  }

  @Override
  public void close() {
    //todo
  }
}
