package de.unistuttgart.isw.sfsc.core.control.bootstrapping;

import com.google.protobuf.ByteString;

public class BootstrapperParameter {
  private final ByteString topic;
  private final int subscriptionPort;

  public BootstrapperParameter(String topic, int subscriptionPort) {
    this.topic = ByteString.copyFromUtf8(topic);
    this.subscriptionPort = subscriptionPort;
  }

  public ByteString getTopic() {
    return topic;
  }

  public int getSubscriptionPort() {
    return subscriptionPort;
  }
}
