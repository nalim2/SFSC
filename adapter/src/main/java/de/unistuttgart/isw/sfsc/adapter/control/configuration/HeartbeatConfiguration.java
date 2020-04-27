package de.unistuttgart.isw.sfsc.adapter.control.configuration;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.heartbeating.HeartbeatParameter;

public final class HeartbeatConfiguration {

  private static final String HEARTBEAT_CORE_TOPIC = "HEARTBEAT";
  private static final String HEARTBEAT_ADAPTER_TOPIC_PREFIX = "HEARTBEAT_CLIENT_";
  private static final int HEARTBEAT_SEND_RATE_MS = 500;
  private static final int EXPECTED_HEARTBEAT_RATE_MS = 2000;

  private final String adapterId;
  private final int sendRateMs;
  private final ByteString adapterTopic;
  private final int expectedIncomingRateMs;
  private final ByteString coreTopic;

  public HeartbeatConfiguration(String adapterId) {
    this.adapterId = adapterId;
    sendRateMs = HEARTBEAT_SEND_RATE_MS;
    adapterTopic = ByteString.copyFromUtf8(HEARTBEAT_ADAPTER_TOPIC_PREFIX + adapterId);
    expectedIncomingRateMs = EXPECTED_HEARTBEAT_RATE_MS;
    coreTopic = ByteString.copyFromUtf8(HEARTBEAT_CORE_TOPIC);
  }

  public HeartbeatParameter toParameter(){
    return new HeartbeatParameter(adapterId, sendRateMs, adapterTopic, expectedIncomingRateMs);
  }

  public String getAdapterId() {
    return adapterId;
  }

  public int getSendRateMs() {
    return sendRateMs;
  }

  public ByteString getAdapterTopic() {
    return adapterTopic;
  }

  public int getExpectedIncomingRateMs() {
    return expectedIncomingRateMs;
  }

  public ByteString getCoreTopic() {
    return coreTopic;
  }

}
