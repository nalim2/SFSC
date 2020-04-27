package de.unistuttgart.isw.sfsc.core.configuration;

import java.util.UUID;

public class DefaultConfiguration {

  private final static String host = "127.0.0.1";
  private final static String backendHost = host;

  private final static int controlPubPort = 1251;
  private final static int controlSubPort = 1252;
  private final static int controlBackendPort = 5701;
  private final static int dataPubPort = 1253;
  private final static int dataSubPort = 1254;
  private final static int dataBackendPort = 1250;

  private final static int heartbeatSendRateMs = 800;
  private final static int heartbeatDeadlineIncomingMs = 2000;

  private final static String bootstrapTopic = "BOOTSTRAP";
  private final static String sessionTopic = "SESSION";
  private final static String heartbeatTopic = "HEARTBEAT";
  private final static String registryQueryTopic = "REGISTRY_QUERY";
  private final static String registryCommandTopic = "REGISTRY_COMMAND";
  private final static String registryPublisherTopic = "REGISTRY_EVENT";


  public String getCoreId() {
    return UUID.randomUUID().toString();
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
