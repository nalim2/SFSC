package de.unistuttgart.isw.sfsc.core.control.session;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.heartbeating.HeartbeatParameter;


public final class HeartbeatConfiguration {

  private static final String HEARTBEAT_CORE_TOPIC = "HEARTBEAT_SERVER";
  private static final int HEARTBEAT_SEND_RATE_MS = 800;
  private static final int EXPECTED_HEARTBEAT_RATE_MS = 2000;

  private final String coreId;
  private final int sendRateMs;
  private final ByteString coreTopic;
  private final int expectedIncomingRateMs;

  public HeartbeatConfiguration(String coreId) {
    this.coreId = coreId;
    sendRateMs = HEARTBEAT_SEND_RATE_MS;
    coreTopic = ByteString.copyFromUtf8(HEARTBEAT_CORE_TOPIC);
    expectedIncomingRateMs = EXPECTED_HEARTBEAT_RATE_MS;
  }

  HeartbeatParameter toParameter() {
    return new HeartbeatParameter(coreId, sendRateMs, coreTopic, expectedIncomingRateMs);
  }

  public String getCoreId() {
    return coreId;
  }

  public int getSendRateMs() {
    return sendRateMs;
  }

  public ByteString getCoreTopic() {
    return coreTopic;
  }

  public int getExpectedIncomingRateMs() {
    return expectedIncomingRateMs;
  }
}
