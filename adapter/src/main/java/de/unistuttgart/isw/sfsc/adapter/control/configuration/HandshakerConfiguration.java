package de.unistuttgart.isw.sfsc.adapter.control.configuration;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.adapter.control.handshake.HandshakerParameter;

public final class HandshakerConfiguration {

  private static final String SESSION_CORE_TOPIC = "SESSION_SERVER";
  private static final String SESSION_ADAPTER_TOPIC_PREFIX = "SESSION_CLIENT_";
  private static final int TIMEOUT_MS = 1000;

  private final String adapterId;
  private final ByteString coreTopic;
  private final ByteString adapterTopic;
  private final int timeoutMs;

  public HandshakerConfiguration(String adapterId) {
    this.adapterId = adapterId;
    coreTopic = ByteString.copyFromUtf8(SESSION_CORE_TOPIC);
    adapterTopic = ByteString.copyFromUtf8(SESSION_ADAPTER_TOPIC_PREFIX + adapterId);
    timeoutMs = TIMEOUT_MS;
  }

  public HandshakerParameter toParameter(){
    return new HandshakerParameter(coreTopic, adapterTopic, timeoutMs);
  }

  public String getAdapterId() {
    return adapterId;
  }

  public ByteString getCoreTopic() {
    return coreTopic;
  }

  public ByteString getAdapterTopic() {
    return adapterTopic;
  }

  public int getTimeoutMs() {
    return timeoutMs;
  }
}
