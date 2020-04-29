package de.unistuttgart.isw.sfsc.adapter.configuration;

import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.TransportProtocol;
import java.util.UUID;

public class DefaultConfiguration {

  private final static TransportProtocol transportProtocol = TransportProtocol.TCP;
  private final static String coreHost = "127.0.0.1";
  private final static int corePubTcpPort = 1251;
  private final static String coreIpcLocation = ".tmp/sfsc/ipc";
  private final static String corePubIpcFile = "controlPub";

  private final static int controlTimeoutMs = 500;
  private final static int heartbeatSendRateMs = 500;
  private final static int heartbeatDeadlineIncomingMs = 2000;
  private final static int registryPollingRateMs = 500;

  private final static String bootstrapCoreTopic = "BOOTSTRAP";
  private final static String handshakeCoreTopic = "SESSION";
  private final static String handshakeAdapterTopic = "SESSION_CLIENT_";
  private final static String heartbeatCoreTopic = "HEARTBEAT";
  private final static String heartbeatAdapterTopic = "HEARTBEAT_CLIENT_";
  private final static String registryCoreQueryTopic = "REGISTRY_QUERY";
  private final static String registryCoreCommandTopic = "REGISTRY_COMMAND";
  private final static String registryCoreEventTopic = "REGISTRY_EVENT";
  private final static String registryAdapterQueryTopic = "REGISTRY_QUERY_CLIENT_";
  private final static String registryAdapterCommandTopic = "REGISTRY_SERVER_CLIENT_";

  private final String adapterId = UUID.randomUUID().toString();

  public String getAdapterId() {
    return adapterId;
  }

  public TransportProtocol getTransportProtocol() {
    return transportProtocol;
  }

  public String getCoreHost() {
    return coreHost;
  }

  public int getCorePubTcpPort() {
    return corePubTcpPort;
  }

  public String getCoreIpcLocation() {
    return coreIpcLocation;
  }

  public String getCorePubIpcFile() {
    return corePubIpcFile;
  }

  public int getControlTimeoutMs() {
    return controlTimeoutMs;
  }

  public int getHeartbeatSendRateMs() {
    return heartbeatSendRateMs;
  }

  public int getHeartbeatDeadlineIncomingMs() {
    return heartbeatDeadlineIncomingMs;
  }

  public int getRegistryPollingRateMs() {
    return registryPollingRateMs;
  }

  public String getBootstrapCoreTopic() {
    return bootstrapCoreTopic;
  }

  public String getHandshakeCoreTopic() {
    return handshakeCoreTopic;
  }

  public String getHandshakeAdapterTopic() {
    return handshakeAdapterTopic + adapterId;
  }

  public String getHeartbeatCoreTopic() {
    return heartbeatCoreTopic;
  }

  public String getHeartbeatAdapterTopic() {
    return heartbeatAdapterTopic + adapterId;
  }

  public String getRegistryCoreQueryTopic() {
    return registryCoreQueryTopic;
  }

  public String getRegistryCoreCommandTopic() {
    return registryCoreCommandTopic;
  }

  public String getRegistryCoreEventTopic() {
    return registryCoreEventTopic;
  }

  public String getRegistryAdapterQueryTopic() {
    return registryAdapterQueryTopic + adapterId;
  }

  public String getRegistryAdapterCommandTopic() {
    return registryAdapterCommandTopic + adapterId;
  }
}
