package de.unistuttgart.isw.sfsc.core;

import java.util.Objects;

public class CoreParameter {

  private final String coreId;
  private final String host;
  private final String backendHost;

  private final int controlPubPort;
  private final int controlSubPort;
  private final int controlBackendPort;
  private final int dataPubPort;
  private final int dataSubPort;
  private final int dataBackendPort;

  private final int heartbeatSendRateMs;
  private final int heartbeatDeadlineIncomingMs;

  private final String bootstrapTopic;
  private final String sessionTopic;
  private final String heartbeatTopic;
  private final String registryQueryTopic;
  private final String registryCommandTopic;
  private final String registryPublisherTopic;

  public CoreParameter(String coreId, String host, String backendHost, int controlPubPort, int controlSubPort, int controlBackendPort,
      int dataPubPort, int dataSubPort, int dataBackendPort, int heartbeatSendRateMs, int heartbeatDeadlineIncomingMs, String bootstrapTopic,
      String sessionTopic, String heartbeatTopic, String registryQueryTopic, String registryCommandTopic, String registryPublisherTopic) {
    Objects.requireNonNull(this.coreId = coreId);
    Objects.requireNonNull(this.host = host);
    Objects.requireNonNull(this.backendHost = backendHost);
    this.controlPubPort = controlPubPort;
    this.controlSubPort = controlSubPort;
    this.controlBackendPort = controlBackendPort;
    this.dataPubPort = dataPubPort;
    this.dataSubPort = dataSubPort;
    this.dataBackendPort = dataBackendPort;
    this.heartbeatSendRateMs = heartbeatSendRateMs;
    this.heartbeatDeadlineIncomingMs = heartbeatDeadlineIncomingMs;
    Objects.requireNonNull(this.bootstrapTopic = bootstrapTopic);
    Objects.requireNonNull(this.sessionTopic = sessionTopic);
    Objects.requireNonNull(this.heartbeatTopic = heartbeatTopic);
    Objects.requireNonNull(this.registryQueryTopic = registryQueryTopic);
    Objects.requireNonNull(this.registryCommandTopic = registryCommandTopic);
    Objects.requireNonNull(this.registryPublisherTopic = registryPublisherTopic);
  }

  public String getCoreId() {
    return coreId;
  }

  public String getHost() {
    return host;
  }

  public String getBackendHost() {
    return backendHost;
  }

  public int getControlPubPort() {
    return controlPubPort;
  }

  public int getControlSubPort() {
    return controlSubPort;
  }

  public int getControlBackendPort() {
    return controlBackendPort;
  }

  public int getDataPubPort() {
    return dataPubPort;
  }

  public int getDataSubPort() {
    return dataSubPort;
  }

  public int getDataBackendPort() {
    return dataBackendPort;
  }

  public int getHeartbeatSendRateMs() {
    return heartbeatSendRateMs;
  }

  public int getHeartbeatDeadlineIncomingMs() {
    return heartbeatDeadlineIncomingMs;
  }

  public String getBootstrapTopic() {
    return bootstrapTopic;
  }

  public String getSessionTopic() {
    return sessionTopic;
  }

  public String getHeartbeatTopic() {
    return heartbeatTopic;
  }

  public String getRegistryQueryTopic() {
    return registryQueryTopic;
  }

  public String getRegistryCommandTopic() {
    return registryCommandTopic;
  }

  public String getRegistryPublisherTopic() {
    return registryPublisherTopic;
  }
}
