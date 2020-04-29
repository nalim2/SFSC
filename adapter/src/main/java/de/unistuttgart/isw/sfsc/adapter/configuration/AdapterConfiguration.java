package de.unistuttgart.isw.sfsc.adapter.configuration;

import de.unistuttgart.isw.sfsc.adapter.AdapterParameter;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.TransportProtocol;
import java.util.Optional;

public class AdapterConfiguration {

  private String adapterId;

  private TransportProtocol transportProtocol;
  private String coreHost;
  private Integer corePubTcpPort;
  private String coreIpcLocation;

  private Integer controlTimeoutMs;
  private Integer heartbeatSendRateMs;
  private Integer heartbeatDeadlineIncomingMs;
  private Integer registryPollingRateMs;

  public AdapterConfiguration setAdapterId(String adapterId) {
    this.adapterId = adapterId;
    return this;
  }

  public AdapterConfiguration setTransportProtocol(TransportProtocol transportProtocol) {
    this.transportProtocol = transportProtocol;
    return this;
  }

  public AdapterConfiguration setCoreHost(String coreHost) {
    this.coreHost = coreHost;
    return this;
  }

  public AdapterConfiguration setCorePubTcpPort(Integer corePubTcpPort) {
    this.corePubTcpPort = corePubTcpPort;
    return this;
  }

  public AdapterConfiguration setCoreIpcLocation(String coreIpcLocation) {
    this.coreIpcLocation = coreIpcLocation;
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
    TransportProtocol transportProtocol = Optional.ofNullable(this.transportProtocol)
        .orElse(defaultConfiguration.getTransportProtocol());
    String coreHost = Optional.ofNullable(this.coreHost)
        .orElse(defaultConfiguration.getCoreHost());
    int corePubTcpPort = Optional.ofNullable(this.corePubTcpPort)
        .orElse(defaultConfiguration.getCorePubTcpPort());
    String coreIpcLocation = Optional.ofNullable(this.coreIpcLocation)
        .orElse(defaultConfiguration.getCoreIpcLocation());
    int controlTimeoutMs = Optional.ofNullable(this.controlTimeoutMs)
        .orElse(defaultConfiguration.getControlTimeoutMs());
    int heartbeatSendRateMs = Optional.ofNullable(this.heartbeatSendRateMs)
        .orElse(defaultConfiguration.getHeartbeatSendRateMs());
    int heartbeatDeadlineIncomingMs = Optional.ofNullable(this.heartbeatDeadlineIncomingMs)
        .orElse(defaultConfiguration.getHeartbeatDeadlineIncomingMs());
    int registryPollingRateMs = Optional.ofNullable(this.registryPollingRateMs)
        .orElse(defaultConfiguration.getRegistryPollingRateMs());

    String corePubIpcFile = defaultConfiguration.getCorePubIpcFile();
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

    return new AdapterParameter(adapterId, transportProtocol, coreHost, corePubTcpPort, coreIpcLocation, corePubIpcFile, controlTimeoutMs,
        heartbeatSendRateMs, heartbeatDeadlineIncomingMs, registryPollingRateMs, bootstrapCoreTopic, handshakeCoreTopic, handshakeAdapterTopic,
        heartbeatCoreTopic, heartbeatAdapterTopic, registryCoreQueryTopic, registryCoreCommandTopic, registryCoreEventTopic,
        registryAdapterQueryTopic, registryAdapterCommandTopic);
  }
}
