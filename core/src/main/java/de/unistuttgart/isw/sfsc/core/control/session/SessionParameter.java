package de.unistuttgart.isw.sfsc.core.control.session;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.heartbeating.HeartbeatParameter;

public class SessionParameter {

  private final String coreId;

  HeartbeatParameter heartbeatParameter;

  private final ByteString sessionTopic;

  public SessionParameter(String coreId, int heartbeatSendRateMs, String heartbeatCoreTopic, int heartbeatDeadlineIncomingMs, String sessionTopic) {
    this.coreId = coreId;
    this.heartbeatParameter = new HeartbeatParameter(coreId, heartbeatSendRateMs, heartbeatCoreTopic, heartbeatDeadlineIncomingMs);
    this.sessionTopic = ByteString.copyFromUtf8(sessionTopic);
  }

  HeartbeatParameter getHeartbeatParameter() {
    return heartbeatParameter;
  }

  public String getCoreId() {
    return coreId;
  }

  public ByteString getSessionTopic() {
    return sessionTopic;
  }
}
