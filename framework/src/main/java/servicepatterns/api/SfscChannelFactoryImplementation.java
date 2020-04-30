package servicepatterns.api;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleServer;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ChannelFactoryTags;
import java.util.Optional;
import java.util.function.Function;
import servicepatterns.api.tagging.ServiceFactory;
import servicepatterns.services.channelfactory.ChannelFactoryServer;

final class SfscChannelFactoryImplementation implements SfscServer {

  private final SfscServiceDescriptor descriptor;
  private final Runnable closeCallback;

  SfscChannelFactoryImplementation(SfscChannelFactoryParameter parameter, ServiceFactory serviceFactory,
      Function<ByteString, SfscPublisher> channelFactory) {
    PubSubConnection pubSubConnection = serviceFactory.pubSubConnection();
    String serviceId = serviceFactory.createServiceId();
    descriptor = SfscServiceDescriptor.newBuilder()
        .setServiceId(serviceId)
        .setAdapterId(serviceFactory.adapterId())
        .setCoreId(serviceFactory.coreId())
        .setServiceName(Optional.ofNullable(parameter.getServiceName()).orElse(serviceId))
        .putAllCustomTags(Optional.ofNullable(parameter.getCustomTags()).orElseGet(serviceFactory::defaultCustomTags))
        .setChannelFactoryTags(ChannelFactoryTags.newBuilder()
            .setInputTopic(Optional.ofNullable(parameter.getInputTopic()).orElseGet(serviceFactory::createTopic))
            .setInputMessageType(Optional.ofNullable(parameter.getInputMessageType()).orElseGet(serviceFactory::defaultType))
            .build())
        .build();

    ChannelFactoryServer channelFactoryServer = new ChannelFactoryServer(channelFactory);
    SimpleServer server = new SimpleServer(pubSubConnection, channelFactoryServer, descriptor.getServerTags().getInputTopic(),
        serviceFactory.executorService());
    Handle handle = serviceFactory.registerService(descriptor);
    closeCallback = () -> {
      handle.close();
      server.close();
    };
  }

  @Override
  public SfscServiceDescriptor getDescriptor() {
    return descriptor;
  }

  @Override
  public void close() {
    closeCallback.run();
  }
}
