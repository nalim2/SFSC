package de.unistuttgart.isw.sfsc.framework.api.services;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.framework.api.registry.ApiRegistryManager;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.types.MessageType;
import de.unistuttgart.isw.sfsc.framework.types.Topic;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class ServiceFactory {

  private static final MessageType defaultType = MessageType.newBuilder().setType(ByteString.EMPTY).build();
  private static final Supplier<String> defaultIdGenerator = () -> UUID.randomUUID().toString();
  private static final Map<String, ByteString> defaultCustomTags = Map.of();
  private final Scheduler scheduler;

  private final PubSubConnection pubSubConnection;
  private final ApiRegistryManager apiRegistryManager;
  private final String coreId;
  private final String adapterId;

  public ServiceFactory(PubSubConnection pubSubConnection, ApiRegistryManager apiRegistryManager, String coreId,
      String adapterId, Scheduler scheduler) {
    this.pubSubConnection = pubSubConnection;
    this.apiRegistryManager = apiRegistryManager;
    this.coreId = coreId;
    this.adapterId = adapterId;
    this.scheduler = scheduler;
  }

  public String adapterId() {
    return adapterId;
  }

  public String coreId() {
    return coreId;
  }

  public PubSubConnection pubSubConnection() {
    return pubSubConnection;
  }

  public String createServiceId() {
    return defaultIdGenerator.get();
  }

  public Topic createTopic() {
    return Topic.newBuilder().setTopic(ByteString.copyFromUtf8(defaultIdGenerator.get())).build();
  }

  public MessageType defaultType() {
    return defaultType;
  }

  public Map<String, ByteString> defaultCustomTags() {
    return defaultCustomTags;
  }

  public Handle registerService(SfscServiceDescriptor descriptor) {
    return apiRegistryManager.registerService(descriptor);
  }

  public Scheduler scheduler() {
    return scheduler;
  }
}
