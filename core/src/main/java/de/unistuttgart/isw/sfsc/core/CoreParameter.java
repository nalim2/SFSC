package de.unistuttgart.isw.sfsc.core;

import java.util.Objects;

public class CoreParameter {

  private final String coreId;
  private final String host;
  private final String backendHost;

  private final int controlPubTcpPort;
  private final int controlSubTcpPort;
  private final int controlBackendTcpPort;
  private final int dataPubTcpPort;
  private final int dataSubTcpPort;
  private final int dataBackendTcpPort;

  private final int heartbeatSendRateMs;
  private final int heartbeatDeadlineIncomingMs;

  private final String ipcFolderLocation;
  private final String controlPubIpcFile;
  private final String controlSubIpcFile;
  private final String dataPubIpcFile;
  private final String dataSubIpcFile;

  private final String bootstrapTopic;
  private final String sessionTopic;
  private final String heartbeatTopic;
  private final String registryQueryTopic;
  private final String registryCommandTopic;
  private final String registryPublisherTopic;

  public CoreParameter(String coreId, String host, String backendHost, int controlPubTcpPort, int controlSubTcpPort, int controlBackendTcpPort,
      int dataPubTcpPort, int dataSubTcpPort, int dataBackendTcpPort, int heartbeatSendRateMs, int heartbeatDeadlineIncomingMs, String ipcFolderLocation,
      String controlPubIpcFile, String controlSubIpcFile, String dataPubIpcFile, String dataSubIpcFile, String bootstrapTopic, String sessionTopic,
      String heartbeatTopic, String registryQueryTopic, String registryCommandTopic,
      String registryPublisherTopic) {
    Objects.requireNonNull(this.coreId = coreId);
    Objects.requireNonNull(this.host = host);
    Objects.requireNonNull(this.backendHost = backendHost);
    this.controlPubTcpPort = controlPubTcpPort;
    this.controlSubTcpPort = controlSubTcpPort;
    this.controlBackendTcpPort = controlBackendTcpPort;
    this.dataPubTcpPort = dataPubTcpPort;
    this.dataSubTcpPort = dataSubTcpPort;
    this.dataBackendTcpPort = dataBackendTcpPort;
    this.heartbeatSendRateMs = heartbeatSendRateMs;
    this.heartbeatDeadlineIncomingMs = heartbeatDeadlineIncomingMs;
    Objects.requireNonNull(this.ipcFolderLocation = ipcFolderLocation);
    Objects.requireNonNull(this.controlPubIpcFile = controlPubIpcFile);
    Objects.requireNonNull(this.controlSubIpcFile = controlSubIpcFile);
    Objects.requireNonNull(this.dataPubIpcFile = dataPubIpcFile);
    Objects.requireNonNull(this.dataSubIpcFile = dataSubIpcFile);
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

  public int getControlPubTcpPort() {
    return controlPubTcpPort;
  }

  public int getControlSubTcpPort() {
    return controlSubTcpPort;
  }

  public int getControlBackendTcpPort() {
    return controlBackendTcpPort;
  }

  public int getDataPubTcpPort() {
    return dataPubTcpPort;
  }

  public int getDataSubTcpPort() {
    return dataSubTcpPort;
  }

  public int getDataBackendTcpPort() {
    return dataBackendTcpPort;
  }

  public int getHeartbeatSendRateMs() {
    return heartbeatSendRateMs;
  }

  public int getHeartbeatDeadlineIncomingMs() {
    return heartbeatDeadlineIncomingMs;
  }

  public String getIpcFolderLocation() {
    return ipcFolderLocation;
  }

  public String getControlPubIpcFile() {
    return controlPubIpcFile;
  }

  public String getControlSubIpcFile() {
    return controlSubIpcFile;
  }

  public String getDataPubIpcFile() {
    return dataPubIpcFile;
  }

  public String getDataSubIpcFile() {
    return dataSubIpcFile;
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
