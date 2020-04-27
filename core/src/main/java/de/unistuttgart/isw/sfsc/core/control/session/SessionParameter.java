package de.unistuttgart.isw.sfsc.core.control.session;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.heartbeating.HeartbeatParameter;

public class SessionParameter {

  private final String coreId;
  private final int heartbeatSendRateMs;
  private final ByteString heartbeatCoreTopic;
  private final int heartbeatDeadlineIncomingMs;

  private final ByteString sessionTopic;
  private final int dataPubPort;
  private final int dataSubPort;

  public SessionParameter(String coreId, int heartbeatSendRateMs, String heartbeatCoreTopic, int heartbeatDeadlineIncomingMs, String sessionTopic,
      int dataPubPort, int dataSubPort) {
    this.coreId = coreId;
    this.heartbeatSendRateMs = heartbeatSendRateMs;
    this.heartbeatCoreTopic = ByteString.copyFromUtf8(heartbeatCoreTopic);
    this.heartbeatDeadlineIncomingMs = heartbeatDeadlineIncomingMs;
    this.sessionTopic = ByteString.copyFromUtf8(sessionTopic);
    this.dataPubPort = dataPubPort;
    this.dataSubPort = dataSubPort;
  }

  HeartbeatParameter heartbeatParameter() {
    return new HeartbeatParameter(coreId, heartbeatSendRateMs, heartbeatCoreTopic, heartbeatDeadlineIncomingMs);
  }

  public String getCoreId() {
    return coreId;
  }

  public int getHeartbeatSendRateMs() {
    return heartbeatSendRateMs;
  }

  public ByteString getHeartbeatCoreTopic() {
    return heartbeatCoreTopic;
  }

  public int getHeartbeatDeadlineIncomingMs() {
    return heartbeatDeadlineIncomingMs;
  }

  public ByteString getSessionTopic() {
    return sessionTopic;
  }

  public int getDataPubPort() {
    return dataPubPort;
  }

  public int getDataSubPort() {
    return dataSubPort;
  }
}
