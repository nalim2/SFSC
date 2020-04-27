package de.unistuttgart.isw.sfsc.adapter.configuration;

import de.unistuttgart.isw.sfsc.adapter.AdapterParameter;
import java.util.Optional;

public class AdapterConfiguration {

  private String adapterId;

  private String coreHost;
  private Integer corePort;

  private Integer controlTimeoutMs;
  private Integer heartbeatSendRateMs;
  private Integer heartbeatDeadlineIncomingMs;
  private Integer registryPollingRateMs;

  public AdapterConfiguration setAdapterId(String adapterId) {
    this.adapterId = adapterId;
    return this;
  }

  public AdapterConfiguration setCoreHost(String coreHost) {
    this.coreHost = coreHost;
    return this;
  }

  public AdapterConfiguration setCorePort(Integer corePort) {
    this.corePort = corePort;
    return this;
  }

  public AdapterConfiguration setControlTimeoutMs(Integer controlTimeoutMs) {
    this.controlTimeoutMs = controlTimeoutMs;
    return this;
  }

  public AdapterConfiguration setHeartbeatSendRateMs(Integer heartbeatSendRateMs) {
    this.heartbeatSendRateMs = heartbeatSendRateMs;
    return this;
  }

  public AdapterConfiguration setHeartbeatDeadlineIncomingMs(Integer heartbeatDeadlineIncomingMs) {
    this.heartbeatDeadlineIncomingMs = heartbeatDeadlineIncomingMs;
    return this;
  }

  public AdapterConfiguration setRegistryPollingRateMs(Integer registryPollingRateMs) {
    this.registryPollingRateMs = registryPollingRateMs;
    return this;
  }

  public AdapterParameter toAdapterParameter() {
    DefaultConfiguration defaultConfiguration = new DefaultConfiguration();

    String adapterId = Optional.ofNullable(this.adapterId)
        .orElse(defaultConfiguration.getAdapterId());
    String coreHost = Optional.ofNullable(this.coreHost)
        .orElse(defaultConfiguration.getCoreHost());
    int corePort = Optional.ofNullable(this.corePort)
        .orElse(defaultConfiguration.getCorePort());
    int controlTimeoutMs = Optional.ofNullable(this.controlTimeoutMs)
        .orElse(defaultConfiguration.getControlTimeoutMs());
    int heartbeatSendRateMs = Optional.ofNullable(this.heartbeatSendRateMs)
        .orElse(defaultConfiguration.getHeartbeatSendRateMs());
    int heartbeatDeadlineIncomingMs = Optional.ofNullable(this.heartbeatDeadlineIncomingMs)
        .orElse(defaultConfiguration.getHeartbeatDeadlineIncomingMs());
    int registryPollingRateMs = Optional.ofNullable(this.registryPollingRateMs)
        .orElse(defaultConfiguration.getRegistryPollingRateMs());

    String bootstrapCoreTopic = defaultConfiguration.getBootstrapCoreTopic();
    String handshakeCoreTopic = defaultConfiguration.getHandshakeCoreTopic();
    String handshakeAdapterTopic = defaultConfiguration.getHandshakeAdapterTopic();
    String heartbeatCoreTopic = defaultConfiguration.getHeartbeatCoreTopic();
    String heartbeatAdapterTopic = defaultConfiguration.getHeartbeatAdapterTopic();
    String registryCoreQueryTopic = defaultConfiguration.getRegistryCoreQueryTopic();
    String registryCoreCommandTopic = defaultConfiguration.getRegistryCoreCommandTopic();
    String registryCoreEventTopic = defaultConfiguration.getRegistryCoreEventTopic();
    String registryAdapterQueryTopic = defaultConfiguration.getRegistryAdapterQueryTopic();
    String registryAdapterCommandTopic = defaultConfiguration.getRegistryAdapterCommandTopic();

    return new AdapterParameter(adapterId, coreHost, corePort, controlTimeoutMs, heartbeatSendRateMs, heartbeatDeadlineIncomingMs,
        registryPollingRateMs, bootstrapCoreTopic, handshakeCoreTopic, handshakeAdapterTopic, heartbeatCoreTopic, heartbeatAdapterTopic,
        registryCoreQueryTopic, registryCoreCommandTopic, registryCoreEventTopic, registryAdapterQueryTopic, registryAdapterCommandTopic);
  }
}
