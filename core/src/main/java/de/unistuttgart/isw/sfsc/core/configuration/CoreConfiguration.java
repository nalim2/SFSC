package de.unistuttgart.isw.sfsc.core.configuration;

import de.unistuttgart.isw.sfsc.core.CoreParameter;
import java.util.Optional;

public class CoreConfiguration {

  private String coreId;
  private String host;
  private String backendHost;

  private Integer controlPubPort;
  private Integer controlSubPort;
  private Integer controlBackendPort;
  private Integer dataPubPort;
  private Integer dataSubPort;
  private Integer dataBackendPort;

  private Integer heartbeatSendRateMs;
  private Integer heartbeatDeadlineIncomingMs;

  public CoreConfiguration setCoreId(String coreId) {
    this.coreId = coreId;
    return this;
  }

  public CoreConfiguration setHost(String host) {
    this.host = host;
    return this;
  }

  public CoreConfiguration setBackendHost(String backendHost) {
    this.backendHost = backendHost;
    return this;
  }

  public CoreConfiguration setControlPubPort(int controlPubPort) {
    this.controlPubPort = controlPubPort;
    return this;
  }

  public CoreConfiguration setControlSubPort(int controlSubPort) {
    this.controlSubPort = controlSubPort;
    return this;
  }

  public CoreConfiguration setControlBackendPort(int controlBackendPort) {
    this.controlBackendPort = controlBackendPort;
    return this;
  }

  public CoreConfiguration setDataPubPort(int dataPubPort) {
    this.dataPubPort = dataPubPort;
    return this;
  }

  public CoreConfiguration setDataSubPort(int dataSubPort) {
    this.dataSubPort = dataSubPort;
    return this;
  }

  public CoreConfiguration setDataBackendPort(int dataBackendPort) {
    this.dataBackendPort = dataBackendPort;
    return this;
  }

  public CoreConfiguration setHeartbeatSendRateMs(int heartbeatSendRateMs) {
    this.heartbeatSendRateMs = heartbeatSendRateMs;
    return this;
  }

  public CoreConfiguration setHeartbeatDeadlineIncomingMs(int heartbeatDeadlineIncomingMs) {
    this.heartbeatDeadlineIncomingMs = heartbeatDeadlineIncomingMs;
    return this;
  }

  public CoreParameter createCoreParameter() {
    EnvironmentReader environmentReader = new EnvironmentReader();
    DefaultConfiguration defaultConfiguration = new DefaultConfiguration();

    String coreId = Optional.ofNullable(this.coreId).or(environmentReader::getCoreId)
        .orElse(defaultConfiguration.getCoreId());
    String frontendHost = Optional.ofNullable(this.host).or(environmentReader::getHost)
        .orElse(defaultConfiguration.getHost());
    String backendHost = Optional.ofNullable(this.backendHost).or(environmentReader::getBackendHost)
        .orElse(defaultConfiguration.getBackendHost());
    int controlSubPort = Optional.ofNullable(this.controlSubPort).or(environmentReader::getControlSubPort)
        .orElse(defaultConfiguration.getControlSubPort());
    int controlPubPort = Optional.ofNullable(this.controlPubPort).or(environmentReader::getControlPubPort)
        .orElse(defaultConfiguration.getControlPubPort());
    int controlBackendPort = Optional.ofNullable(this.controlBackendPort).or(environmentReader::getControlBackendPort)
        .orElse(defaultConfiguration.getControlBackendPort());
    int dataPubPort = Optional.ofNullable(this.dataPubPort).or(environmentReader::getDataPubPort)
        .orElse(defaultConfiguration.getDataPubPort());
    int dataSubPort = Optional.ofNullable(this.dataSubPort).or(environmentReader::getDataSubPort)
        .orElse(defaultConfiguration.getDataSubPort());
    int dataBackendPort = Optional.ofNullable(this.dataBackendPort).or(environmentReader::getDataBackendPort)
        .orElse(defaultConfiguration.getDataBackendPort());
    int heartbeatSendRateMs = Optional.ofNullable(this.heartbeatSendRateMs).or(environmentReader::getHeartbeatSendRateMs)
        .orElse(defaultConfiguration.getHeartbeatSendRateMs());
    int heartbeatDeadlineIncomingMs = Optional.ofNullable(this.heartbeatDeadlineIncomingMs).or(environmentReader::getHeartbeatDeadlineIncomingMs)
        .orElse(defaultConfiguration.getHeartbeatDeadlineIncomingMs());

    String bootstrapTopic = defaultConfiguration.getBootstrapTopic();
    String sessionTopic = defaultConfiguration.getSessionTopic();
    String heartbeatTopic = defaultConfiguration.getHeartbeatTopic();
    String registryQueryTopic = defaultConfiguration.getRegistryQueryTopic();
    String registryCommandTopic = defaultConfiguration.getRegistryCommandTopic();
    String registryPublisherTopic = defaultConfiguration.getRegistryPublisherTopic();

    return new CoreParameter(coreId, frontendHost, backendHost, controlPubPort, controlSubPort, controlBackendPort, dataPubPort, dataSubPort,
        dataBackendPort, heartbeatSendRateMs, heartbeatDeadlineIncomingMs, bootstrapTopic, sessionTopic, heartbeatTopic, registryQueryTopic,
        registryCommandTopic, registryPublisherTopic);
  }
}
