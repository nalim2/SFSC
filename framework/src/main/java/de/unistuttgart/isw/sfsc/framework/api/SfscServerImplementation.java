package de.unistuttgart.isw.sfsc.framework.api;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.framework.api.tagging.ServiceFactory;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServerTags;
import de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep.AckServer;
import de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep.AckServerResult;
import java.util.Optional;
import java.util.function.Function;

final class SfscServerImplementation implements SfscServer {

  private static final int defaultSendMaxTries = 3;

  private final SfscServiceDescriptor descriptor;
  private final Runnable closeCallback;

  SfscServerImplementation(SfscServerParameter parameter, ServiceFactory serviceFactory, Function<ByteString, AckServerResult> serverFunction) {
    PubSubConnection pubSubConnection = serviceFactory.pubSubConnection();
    String serviceId = serviceFactory.createServiceId();
    descriptor = SfscServiceDescriptor.newBuilder()
        .setServiceId(serviceId)
        .setAdapterId(serviceFactory.adapterId())
        .setCoreId(serviceFactory.coreId())
        .setServiceName(Optional.ofNullable(parameter.getServiceName()).orElse(serviceId))
        .putAllCustomTags(Optional.ofNullable(parameter.getCustomTags()).orElseGet(serviceFactory::defaultCustomTags))
        .setServerTags(ServerTags.newBuilder()
            .setInputTopic(Optional.ofNullable(parameter.getInputTopic()).orElseGet(serviceFactory::createTopic))
            .setInputMessageType(Optional.ofNullable(parameter.getInputMessageType()).orElseGet(serviceFactory::defaultType))
            .setOutputMessageType(Optional.ofNullable(parameter.getOutputMessageType()).orElseGet(serviceFactory::defaultType))
            .setRegex(parameter.getRegexDefinition())
            .setTimeoutMs(Optional.ofNullable(parameter.getTimeoutMs()).orElseGet(serviceFactory::defaultTimeoutMs))
            .setSendRateMs(Optional.ofNullable(parameter.getSendRateMs()).orElseGet(serviceFactory::defaultTimeoutMs))
            .setSendMaxTries(Optional.ofNullable(parameter.getSendMaxTries()).orElse(defaultSendMaxTries))
            .build())
        .build();

    AckServer server = new AckServer(pubSubConnection,
        serviceFactory.executorService(),
        serverFunction,
        descriptor.getServerTags().getInputTopic(),
        descriptor.getServerTags().getTimeoutMs(),
        descriptor.getServerTags().getSendRateMs(),
        descriptor.getServerTags().getSendMaxTries(),
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
