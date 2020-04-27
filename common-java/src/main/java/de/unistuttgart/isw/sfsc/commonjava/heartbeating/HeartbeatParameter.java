package de.unistuttgart.isw.sfsc.commonjava.heartbeating;

import com.google.protobuf.ByteString;

public final class HeartbeatParameter {

  private final String outgoingId;
  private final ByteString incomingTopic;
  private final int sendRateMs;
  private final int heartbeatDeadlineIncomingMs;

  public HeartbeatParameter(String outgoingId, int SendRateMs, String incomingTopic, int heartbeatDeadlineIncomingMs) {
    this.outgoingId = outgoingId;
    this.incomingTopic = ByteString.copyFromUtf8(incomingTopic);
    this.sendRateMs = SendRateMs;
    this.heartbeatDeadlineIncomingMs = heartbeatDeadlineIncomingMs;
  }

  public String getOutgoingId() {
    return outgoingId;
  }

  public int getSendRateMs() {
    return sendRateMs;
  }

  public ByteString getExpectedIncomingTopic() {
    return incomingTopic;
  }

  public int getHeartbeatDeadlineIncomingMs() {
    return heartbeatDeadlineIncomingMs;
  }
}
