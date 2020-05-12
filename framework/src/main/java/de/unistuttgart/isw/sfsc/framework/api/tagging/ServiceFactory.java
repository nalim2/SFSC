package de.unistuttgart.isw.sfsc.framework.api.tagging;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceFactory {

  private static final Logger logger = LoggerFactory.getLogger(ServiceFactory.class);

  private final ScheduledExecutorService executorService = Executors.unconfigurableScheduledExecutorService(
      Executors.newScheduledThreadPool(1, new ExceptionLoggingThreadFactory(getClass().getName(), logger)));

  private final PubSubConnection pubSubConnection;
  private final ApiRegistryManager apiRegistryManager;
  private final String coreId;
  private final String adapterId;

  public ServiceFactory(PubSubConnection pubSubConnection, ApiRegistryManager apiRegistryManager, String coreId, String adapterId) {
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
    return UUID.randomUUID().toString();
  }

  public ByteString createTopic() {
    return ByteString.copyFromUtf8(UUID.randomUUID().toString());
  }

  public ByteString defaultType() {
    return ByteString.EMPTY;
  }

  public Map<String, ByteString> defaultCustomTags() {
    return Map.of();
  }

  public int defaultTimeoutMs() {
    return 1000;
  }

  public Handle registerService(SfscServiceDescriptor descriptor) {
    return apiRegistryManager.registerService(descriptor);
  }

  public ScheduledExecutorService executorService() {
    return executorService;
  }
}
