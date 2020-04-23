package de.unistuttgart.isw.sfsc.commonjava.heartbeating;

import com.google.protobuf.ByteString;

public final class HeartbeatParameter {

  private final String outgoingId;
  private final ByteString incomingTopic;
  private final int sendRateMs;
  private final int expectedIncomingRateMs;

  public HeartbeatParameter(String outgoingId, int SendRateMs, ByteString incomingTopic, int expectedIncomingRateMs) {
    this.outgoingId = outgoingId;
    this.incomingTopic = incomingTopic;
    this.sendRateMs = SendRateMs;
    this.expectedIncomingRateMs = expectedIncomingRateMs;
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

  public int getExpectedIncomingRateMs() {
    return expectedIncomingRateMs;
  }
}
