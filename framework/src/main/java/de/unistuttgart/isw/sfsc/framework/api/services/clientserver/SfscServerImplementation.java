package de.unistuttgart.isw.sfsc.framework.api.services.clientserver;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.framework.api.services.ServiceFactory;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServiceTags;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServiceTags.ServerTags;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServiceTags.ServerTags.AckSettings;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServiceTags.ServerTags.RegexDefinition;
import de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep.AckServer;
import de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep.AckServerResult;
import de.unistuttgart.isw.sfsc.framework.types.MessageType;
import de.unistuttgart.isw.sfsc.framework.types.SfscId;
import de.unistuttgart.isw.sfsc.framework.types.Topic;
import java.util.Optional;
import java.util.function.Function;

public final class SfscServerImplementation implements SfscServer {

  private static final int defaultSendMaxTries = 1;
  private static final int defaultTimeout = 1000;
  private static final RegexDefinition defaultRegex = RegexDefinition.getDefaultInstance();

  private final SfscServiceDescriptor descriptor;
  private final Runnable closeCallback;

  public SfscServerImplementation(SfscServerParameter parameter, ServiceFactory serviceFactory,
      Function<ByteString, AckServerResult> serverFunction) {
    PubSubConnection pubSubConnection = serviceFactory.pubSubConnection();
    String serviceId = serviceFactory.createServiceId();
    MessageType inputMessageType = parameter.getInputMessageType() == null ? serviceFactory.defaultType() : MessageType.newBuilder().setType(parameter.getInputMessageType()).build();
    MessageType outputMessageType = parameter.getOutputMessageType() == null ? serviceFactory.defaultType() : MessageType.newBuilder().setType(parameter.getOutputMessageType()).build();
    Topic topic = parameter.getInputTopic() == null ? serviceFactory.createTopic() : Topic.newBuilder().setTopic(parameter.getInputTopic()).build();
    descriptor = SfscServiceDescriptor
        .newBuilder()
        .setServiceId(SfscId.newBuilder().setId(serviceId).build())
        .setAdapterId(SfscId.newBuilder().setId(serviceFactory.adapterId()).build())
        .setCoreId(SfscId.newBuilder().setId(serviceFactory.coreId()).build())
        .setServiceName(Optional.ofNullable(parameter.getServiceName()).orElse(serviceId))
        .putAllCustomTags(Optional.ofNullable(parameter.getCustomTags()).orElseGet(serviceFactory::defaultCustomTags))
        .setServiceTags(ServiceTags.newBuilder().setServerTags(ServerTags
            .newBuilder()
            .setInputTopic(topic)
            .setInputMessageType(inputMessageType)
            .setOutputMessageType(outputMessageType)
            .setRegex(Optional.ofNullable(parameter.getRegexDefinition()).orElse(defaultRegex))
            .setAckSettings(AckSettings
                .newBuilder()
                .setTimeoutMs(Optional.ofNullable(parameter.getTimeoutMs()).orElse(defaultTimeout))
                .setSendMaxTries(Optional.ofNullable(parameter.getSendMaxTries()).orElse(defaultSendMaxTries))
                .build())
            .build()).build()
  )
        .build();

    AckServer server = new AckServer(
        pubSubConnection,
        serviceFactory.scheduler(),
        serverFunction,
        descriptor.getServiceTags().getServerTags().getInputTopic().getTopic(),
        descriptor.getServiceTags().getServerTags().getAckSettings().getTimeoutMs(),
        descriptor.getServiceTags().getServerTags().getAckSettings().getTimeoutMs(),
        descriptor.getServiceTags().getServerTags().getAckSettings().getSendMaxTries()
    );

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
