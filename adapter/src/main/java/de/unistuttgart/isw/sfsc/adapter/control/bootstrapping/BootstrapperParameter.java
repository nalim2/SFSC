package de.unistuttgart.isw.sfsc.adapter.control.bootstrapping;

import com.google.protobuf.ByteString;

public class BootstrapperParameter {
  private final ByteString remoteTopic;
  private final int timeoutMs;

  public BootstrapperParameter(String remoteTopic, int timeoutMs) {
    this.remoteTopic = ByteString.copyFromUtf8(remoteTopic);
    this.timeoutMs = timeoutMs;
  }

  public ByteString getRemoteTopic() {
    return remoteTopic;
  }

  public int getTimeoutMs() {
    return timeoutMs;
  }
}
