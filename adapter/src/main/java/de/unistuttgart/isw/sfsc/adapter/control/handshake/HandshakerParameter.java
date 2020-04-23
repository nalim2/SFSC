package de.unistuttgart.isw.sfsc.adapter.control.handshake;

import com.google.protobuf.ByteString;

public class HandshakerParameter {

  private final ByteString sessionRemoteTopic;
  private final ByteString sessionLocalTopic;
  private final int timeoutMs;

  public HandshakerParameter(ByteString sessionRemoteTopic, ByteString sessionLocalTopic, int timeoutMs) {
    this.sessionRemoteTopic = sessionRemoteTopic;
    this.sessionLocalTopic = sessionLocalTopic;
    this.timeoutMs = timeoutMs;
  }

  public ByteString getSessionRemoteTopic() {
    return sessionRemoteTopic;
  }

  public ByteString getSessionLocalTopic() {
    return sessionLocalTopic;
  }

  public int getTimeoutMs() {
    return timeoutMs;
  }
}
