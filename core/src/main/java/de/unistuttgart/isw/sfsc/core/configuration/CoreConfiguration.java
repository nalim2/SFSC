package de.unistuttgart.isw.sfsc.core.configuration;

import de.unistuttgart.isw.sfsc.core.CoreParameter;
import java.util.Optional;

public class CoreConfiguration {

  private String coreId;
  private String host;
  private String backendHost;

  private Integer controlPubTcpPort;
  private Integer controlSubTcpPort;
  private Integer controlBackendTcpPort;
  private Integer dataPubTcpPort;
  private Integer dataSubTcpPort;
  private Integer dataBackendTcpPort;

  private Integer heartbeatSendRateMs;
  private Integer heartbeatDeadlineIncomingMs;

  private String ipcFolderLocation;

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

  public CoreConfiguration setControlPubTcpPort(Integer controlPubTcpPort) {
    this.controlPubTcpPort = controlPubTcpPort;
    return this;
  }

  public CoreConfiguration setControlSubTcpPort(Integer controlSubTcpPort) {
    this.controlSubTcpPort = controlSubTcpPort;
    return this;
  }

  public CoreConfiguration setControlBackendTcpPort(Integer controlBackendTcpPort) {
    this.controlBackendTcpPort = controlBackendTcpPort;
    return this;
  }

  public CoreConfiguration setDataPubTcpPort(Integer dataPubTcpPort) {
    this.dataPubTcpPort = dataPubTcpPort;
    return this;
  }

  public CoreConfiguration setDataSubTcpPort(Integer dataSubTcpPort) {
    this.dataSubTcpPort = dataSubTcpPort;
    return this;
  }

  public CoreConfiguration setDataBackendTcpPort(Integer dataBackendTcpPort) {
    this.dataBackendTcpPort = dataBackendTcpPort;
    return this;
  }

  public CoreConfiguration setHeartbeatSendRateMs(Integer heartbeatSendRateMs) {
    this.heartbeatSendRateMs = heartbeatSendRateMs;
    return this;
  }

  public CoreConfiguration setHeartbeatDeadlineIncomingMs(Integer heartbeatDeadlineIncomingMs) {
    this.heartbeatDeadlineIncomingMs = heartbeatDeadlineIncomingMs;
    return this;
  }

  public CoreConfiguration setIpcFolderLocation(String ipcFolderLocation) {
    this.ipcFolderLocation = ipcFolderLocation;
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
    int controlSubTcpPort = Optional.ofNullable(this.controlSubTcpPort).or(environmentReader::getControlSubPort)
        .orElse(defaultConfiguration.getControlSubTcpPort());
    int controlPubTcpPort = Optional.ofNullable(this.controlPubTcpPort).or(environmentReader::getControlPubPort)
        .orElse(defaultConfiguration.getControlPubTcpPort());
    int controlBackendTcpPort = Optional.ofNullable(this.controlBackendTcpPort).or(environmentReader::getControlBackendPort)
        .orElse(defaultConfiguration.getControlBackendTcpPort());
    int dataPubTcpPort = Optional.ofNullable(this.dataPubTcpPort).or(environmentReader::getDataPubPort)
        .orElse(defaultConfiguration.getDataPubTcpPort());
    int dataSubTcpPort = Optional.ofNullable(this.dataSubTcpPort).or(environmentReader::getDataSubPort)
        .orElse(defaultConfiguration.getDataSubTcpPort());
    int dataBackendTcpPort = Optional.ofNullable(this.dataBackendTcpPort).or(environmentReader::getDataBackendPort)
        .orElse(defaultConfiguration.getDataBackendTcpPort());
    int heartbeatSendRateMs = Optional.ofNullable(this.heartbeatSendRateMs).or(environmentReader::getHeartbeatSendRateMs)
        .orElse(defaultConfiguration.getHeartbeatSendRateMs());
    int heartbeatDeadlineIncomingMs = Optional.ofNullable(this.heartbeatDeadlineIncomingMs).or(environmentReader::getHeartbeatDeadlineIncomingMs)
        .orElse(defaultConfiguration.getHeartbeatDeadlineIncomingMs());
    String ipcFolderLocation = Optional.ofNullable(this.ipcFolderLocation).or(environmentReader::getIpcFolderLocation)
        .orElse(defaultConfiguration.getIpcFolderLocation());

    String controlPubIpcFile = defaultConfiguration.getControlPubIpcFile();
    String controlSubIpcFile = defaultConfiguration.getControlSubIpcFile();
    String dataPubIpcFile = defaultConfiguration.getDataPubIpcFile();
    String dataSubIpcFile = defaultConfiguration.getDataSubIpcFile();
    String bootstrapTopic = defaultConfiguration.getBootstrapTopic();
    String sessionTopic = defaultConfiguration.getSessionTopic();
    String heartbeatTopic = defaultConfiguration.getHeartbeatTopic();
    String registryQueryTopic = defaultConfiguration.getRegistryQueryTopic();
    String registryCommandTopic = defaultConfiguration.getRegistryCommandTopic();
    String registryPublisherTopic = defaultConfiguration.getRegistryPublisherTopic();

    return new CoreParameter(coreId, frontendHost, backendHost, controlPubTcpPort, controlSubTcpPort, controlBackendTcpPort, dataPubTcpPort,
        dataSubTcpPort, dataBackendTcpPort, heartbeatSendRateMs, heartbeatDeadlineIncomingMs, ipcFolderLocation, controlPubIpcFile, controlSubIpcFile,
        dataPubIpcFile, dataSubIpcFile, bootstrapTopic, sessionTopic, heartbeatTopic, registryQueryTopic, registryCommandTopic,
        registryPublisherTopic);
  }
}
