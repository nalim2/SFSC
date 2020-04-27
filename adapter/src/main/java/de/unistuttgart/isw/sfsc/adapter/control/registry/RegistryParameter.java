package de.unistuttgart.isw.sfsc.adapter.control.registry;

import com.google.protobuf.ByteString;

public class RegistryParameter {
  private final String adapterId;
  private final ByteString coreQueryTopic;
  private final ByteString coreCommandTopic;
  private final ByteString coreEventPublisherTopic;
  private final ByteString adapterQueryTopic;
  private final ByteString adapterCommandTopic;
  private final int timeoutMs;
  private final int pollingRateMs;

  public RegistryParameter(String adapterId, ByteString coreQueryTopic, ByteString coreCommandTopic, ByteString coreEventPublisherTopic,
      ByteString adapterQueryTopic, ByteString adapterCommandTopic, int timeoutMs, int pollingRateMs) {
    this.adapterId = adapterId;
    this.coreQueryTopic = coreQueryTopic;
    this.coreCommandTopic = coreCommandTopic;
    this.coreEventPublisherTopic = coreEventPublisherTopic;
    this.adapterQueryTopic = adapterQueryTopic;
    this.adapterCommandTopic = adapterCommandTopic;
    this.timeoutMs = timeoutMs;
    this.pollingRateMs = pollingRateMs;
  }

  public String getAdapterId() {
    return adapterId;
  }

  public ByteString getCoreQueryTopic() {
    return coreQueryTopic;
  }

  public ByteString getCoreCommandTopic() {
    return coreCommandTopic;
  }

  public ByteString getCoreEventPublisherTopic() {
    return coreEventPublisherTopic;
  }

  public ByteString getAdapterQueryTopic() {
    return adapterQueryTopic;
  }

  public ByteString getAdapterCommandTopic() {
    return adapterCommandTopic;
  }

  public int getTimeoutMs() {
    return timeoutMs;
  }

  public int getPollingRateMs() {
    return pollingRateMs;
  }
}
