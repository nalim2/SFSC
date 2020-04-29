package de.unistuttgart.isw.sfsc.core.configuration;

import java.util.UUID;

public class DefaultConfiguration {

  private final static String host = "127.0.0.1";
  private final static String backendHost = host;

  private final static int controlPubTcpPort = 1251;
  private final static int controlSubTcpPort = 1252;
  private final static int controlBackendTcpPort = 5701;
  private final static int dataPubTcpPort = 1253;
  private final static int dataSubTcpPort = 1254;
  private final static int dataBackendTcpPort = 1250;

  private final static int heartbeatSendRateMs = 800;
  private final static int heartbeatDeadlineIncomingMs = 2000;

  private final static String ipcFolderLocation = "./tmp/sfsc/ipc";
  private final static String controlPubIpcFile = "controlPub";
  private final static String controlSubIpcFile = "controlSub";
  private final static String dataPubIpcFile = "dataPub";
  private final static String dataSubIpcFile = "dataSub";

  private final static String bootstrapTopic = "BOOTSTRAP";
  private final static String sessionTopic = "SESSION";
  private final static String heartbeatTopic = "HEARTBEAT";
  private final static String registryQueryTopic = "REGISTRY_QUERY";
  private final static String registryCommandTopic = "REGISTRY_COMMAND";
  private final static String registryPublisherTopic = "REGISTRY_EVENT";


  private final String coreId = UUID.randomUUID().toString();

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

  public String getIpcFolderLocation() {return ipcFolderLocation;}

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

  public int getHeartbeatSendRateMs() {
    return heartbeatSendRateMs;
  }

  public int getHeartbeatDeadlineIncomingMs() {
    return heartbeatDeadlineIncomingMs;
  }

}
