package de.unistuttgart.isw.sfsc.framework.api.services.clientserver;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.framework.api.services.ServiceFactory;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServerTags;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServerTags.AckSettings;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServerTags.RegexDefinition;
import de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep.AckServer;
import de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep.AckServerResult;
import java.util.Optional;
import java.util.function.Function;

public final class SfscServerImplementation implements SfscServer {

  private static final int defaultSendMaxTries = 1;
  private static final RegexDefinition defaultRegex = RegexDefinition.getDefaultInstance();

  private final SfscServiceDescriptor descriptor;
  private final Runnable closeCallback;

  public SfscServerImplementation(SfscServerParameter parameter, ServiceFactory serviceFactory,
      Function<ByteString, AckServerResult> serverFunction) {
    PubSubConnection pubSubConnection = serviceFactory.pubSubConnection();
    String serviceId = serviceFactory.createServiceId();
    descriptor = SfscServiceDescriptor
        .newBuilder()
        .setServiceId(serviceId)
        .setAdapterId(serviceFactory.adapterId())
        .setCoreId(serviceFactory.coreId())
        .setServiceName(Optional.ofNullable(parameter.getServiceName()).orElse(serviceId))
        .putAllCustomTags(Optional.ofNullable(parameter.getCustomTags()).orElseGet(serviceFactory::defaultCustomTags))
        .setServerTags(ServerTags
            .newBuilder()
            .setInputTopic(Optional.ofNullable(parameter.getInputTopic()).orElseGet(serviceFactory::createTopic))
            .setInputMessageType(Optional
                .ofNullable(parameter.getInputMessageType())
                .orElseGet(serviceFactory::defaultType))
            .setOutputMessageType(Optional
                .ofNullable(parameter.getOutputMessageType())
                .orElseGet(serviceFactory::defaultType))
            .setRegex(Optional.ofNullable(parameter.getRegexDefinition()).orElse(defaultRegex))
            .setAckSettings(AckSettings
                .newBuilder()
                .setTimeoutMs(Optional.ofNullable(parameter.getTimeoutMs()).orElseGet(serviceFactory::defaultTimeoutMs))
                .setSendMaxTries(Optional.ofNullable(parameter.getSendMaxTries()).orElse(defaultSendMaxTries))
                .build())
            .build())
        .build();

    AckServer server = new AckServer(
        pubSubConnection,
        serviceFactory.executorService(),
        serverFunction,
        descriptor.getServerTags().getInputTopic(),
        descriptor.getServerTags().getAckSettings().getTimeoutMs(),
        descriptor.getServerTags().getAckSettings().getTimeoutMs(),
        descriptor.getServerTags().getAckSettings().getSendMaxTries(),
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
