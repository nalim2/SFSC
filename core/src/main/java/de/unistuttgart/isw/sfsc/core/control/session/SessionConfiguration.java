package de.unistuttgart.isw.sfsc.core.control.session;

import com.google.protobuf.ByteString;

public final class SessionConfiguration {

  private static final String SESSION_TOPIC = "SESSION_SERVER";

  private final String coreId;
  private final ByteString sessionTopic;
  private final int publishedDataPubPort;
  private final int publishedDataSubPort;

  public SessionConfiguration(String coreId, int dataPubPort, int dataSubPort) {
    this.coreId = coreId;
    this.publishedDataPubPort = dataPubPort;
    this.publishedDataSubPort = dataSubPort;
    sessionTopic = ByteString.copyFromUtf8(SESSION_TOPIC);
  }

  public String getCoreId() {
    return coreId;
  }

  public ByteString getSessionTopic() {
    return sessionTopic;
  }

  public int getPublishedDataPubPort() {
    return publishedDataPubPort;
  }

  public int getPublishedDataSubPort() {
    return publishedDataSubPort;
  }
}



