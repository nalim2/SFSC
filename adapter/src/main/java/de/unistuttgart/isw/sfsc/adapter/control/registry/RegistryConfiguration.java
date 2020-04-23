package de.unistuttgart.isw.sfsc.adapter.control.registry;

import com.google.protobuf.ByteString;

public final class RegistryConfiguration {

  private static final String REGISTRY_CORE_QUERY_TOPIC = "REGISTRY_QUERY_SERVER";
  private static final String REGISTRY_CORE_COMMAND_TOPIC = "REGISTRY_COMMAND_SERVER";
  private static final String REGISTRY_CORE_EVENT_PUBLISHER_TOPIC = "REGISTRY_EVENT_PUBLISHER";
  private static final String REGISTRY_ADAPTER_QUERY_TOPIC_PREFIX = "REGISTRY_QUERY_CLIENT_";
  private static final String REGISTRY_ADAPTER_COMMAND_TOPIC_PREFIX = "REGISTRY_SERVER_CLIENT_";
  private static final int POLLING_RATE_MS = 1000;
  private static final int TIMEOUT_MS = 1000;

  private final String adapterId;
  private final ByteString registryCoreQueryTopic;
  private final ByteString registryCoreCommandTopic;
  private final ByteString registryCoreEventPublisherTopic;
  private final ByteString registryAdapterQueryTopic;
  private final ByteString registryAdapterCommandTopic;
  private final int timeoutMs;
  private final int pollingRateMs;

  public RegistryConfiguration(String adapterId) {
    this.adapterId = adapterId;
    registryCoreQueryTopic = ByteString.copyFromUtf8(REGISTRY_CORE_QUERY_TOPIC);
    registryCoreCommandTopic = ByteString.copyFromUtf8(REGISTRY_CORE_COMMAND_TOPIC);
    registryCoreEventPublisherTopic = ByteString.copyFromUtf8(REGISTRY_CORE_EVENT_PUBLISHER_TOPIC);
    registryAdapterQueryTopic = ByteString.copyFromUtf8(REGISTRY_ADAPTER_QUERY_TOPIC_PREFIX + adapterId);
    registryAdapterCommandTopic = ByteString.copyFromUtf8(REGISTRY_ADAPTER_COMMAND_TOPIC_PREFIX + adapterId);
    timeoutMs = TIMEOUT_MS;
    pollingRateMs = POLLING_RATE_MS;
  }

  public String getAdapterId() {
    return adapterId;
  }

  public ByteString getRegistryCoreQueryTopic() {
    return registryCoreQueryTopic;
  }

  public ByteString getRegistryCoreCommandTopic() {
    return registryCoreCommandTopic;
  }

  public ByteString getRegistryCoreEventPublisherTopic() {
    return registryCoreEventPublisherTopic;
  }

  public ByteString getRegistryAdapterQueryTopic() {
    return registryAdapterQueryTopic;
  }

  public ByteString getRegistryAdapterCommandTopic() {
    return registryAdapterCommandTopic;
  }

  public int getTimeoutMs() {
    return timeoutMs;
  }

  public int getPollingRateMs() {
    return pollingRateMs;
  }
}
