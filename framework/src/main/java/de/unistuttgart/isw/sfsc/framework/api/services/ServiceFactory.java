package de.unistuttgart.isw.sfsc.framework.api.services;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.framework.api.registry.ApiRegistryManager;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceFactory {

  private static final Logger logger = LoggerFactory.getLogger(ServiceFactory.class);

  private static final ByteString defaultType = ByteString.EMPTY;
  private static final Supplier<String> defaultIdGenerator = () -> UUID.randomUUID().toString();
  private static final Map<String, ByteString> defaultCustomTags = Map.of();
  private final ScheduledExecutorService executorService = Executors.unconfigurableScheduledExecutorService(
      Executors.newScheduledThreadPool(1, new ExceptionLoggingThreadFactory(getClass().getName(), logger)));

  private final PubSubConnection pubSubConnection;
  private final ApiRegistryManager apiRegistryManager;
  private final String coreId;
  private final String adapterId;

  public ServiceFactory(PubSubConnection pubSubConnection, ApiRegistryManager apiRegistryManager, String coreId,
      String adapterId) {
    this.pubSubConnection = pubSubConnection;
    this.apiRegistryManager = apiRegistryManager;
    this.coreId = coreId;
    this.adapterId = adapterId;
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

  public ByteString createTopic() {
    return ByteString.copyFromUtf8(defaultIdGenerator.get());
  }

  public ByteString defaultType() {
    return defaultType;
  }

  public Map<String, ByteString> defaultCustomTags() {
    return defaultCustomTags;
  }

  public Handle registerService(SfscServiceDescriptor descriptor) {
    return apiRegistryManager.registerService(descriptor);
  }

  public ScheduledExecutorService executorService() {
    return executorService;
  }
}
