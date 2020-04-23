package de.unistuttgart.isw.sfsc.adapter.control.bootstrapping;

import com.google.protobuf.ByteString;

public class BootstrapperParameter {
  private final ByteString remoteTopic;
  private final int timeoutMs;

  public BootstrapperParameter(ByteString remoteTopic, int timeoutMs) {
    this.remoteTopic = remoteTopic;
    this.timeoutMs = timeoutMs;
  }

  public ByteString getRemoteTopic() {
    return remoteTopic;
  }

  public int getTimeoutMs() {
    return timeoutMs;
  }
}
