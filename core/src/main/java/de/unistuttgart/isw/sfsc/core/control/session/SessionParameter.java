package de.unistuttgart.isw.sfsc.core.control.session;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.heartbeating.HeartbeatParameter;

public class SessionParameter {

  private final String coreId;

  HeartbeatParameter heartbeatParameter;

  private final ByteString sessionTopic;
  private final int dataPubPort;
  private final int dataSubPort;

  public SessionParameter(String coreId, int heartbeatSendRateMs, String heartbeatCoreTopic, int heartbeatDeadlineIncomingMs, String sessionTopic,
      int dataPubPort, int dataSubPort) {
    this.coreId = coreId;
    this.heartbeatParameter = new HeartbeatParameter(coreId, heartbeatSendRateMs, heartbeatCoreTopic, heartbeatDeadlineIncomingMs);
    this.sessionTopic = ByteString.copyFromUtf8(sessionTopic);
    this.dataPubPort = dataPubPort;
    this.dataSubPort = dataSubPort;
  }

  HeartbeatParameter heartbeatParameter() {
    return heartbeatParameter;
  }

  public String getCoreId() {
    return coreId;
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
