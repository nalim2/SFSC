package de.unistuttgart.isw.sfsc.adapter.control.registry;

import com.google.protobuf.ByteString;

public class RegistryParameter {

  private final String adapterId;
  private final ByteString coreQueryTopic;
  private final ByteString coreCommandTopic;
  private final ByteString coreEventTopic;
  private final ByteString adapterQueryTopic;
  private final ByteString adapterCommandTopic;
  private final int timeoutMs;
  private final int pollingRateMs;

  public RegistryParameter(String adapterId, String coreQueryTopic, String coreCommandTopic, String coreEventTopic, String adapterQueryTopic,
      String adapterCommandTopic, int timeoutMs, int pollingRateMs) {
    this.adapterId = adapterId;
    this.coreQueryTopic = ByteString.copyFromUtf8(coreQueryTopic);
    this.coreCommandTopic = ByteString.copyFromUtf8(coreCommandTopic);
    this.coreEventTopic = ByteString.copyFromUtf8(coreEventTopic);
    this.adapterQueryTopic = ByteString.copyFromUtf8(adapterQueryTopic);
    this.adapterCommandTopic = ByteString.copyFromUtf8(adapterCommandTopic);
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

  public ByteString getCoreEventTopic() {
    return coreEventTopic;
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
