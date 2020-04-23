package de.unistuttgart.isw.sfsc.adapter.control.configuration;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.adapter.control.bootstrapping.BootstrapperParameter;

public final class BootstrapperConfiguration {

  private static final String BOOTSTRAPPING_CORE_TOPIC = "BOOTSTRAP";
  private static final int BOOTSTRAP_TIMEOUT_MS = 1000;

  private final ByteString bootstrappingCoreTopic;
  private final int bootstrapTimeoutMs;

  public BootstrapperConfiguration() {
    bootstrappingCoreTopic = ByteString.copyFromUtf8(BOOTSTRAPPING_CORE_TOPIC);
    bootstrapTimeoutMs = BOOTSTRAP_TIMEOUT_MS;
  }

  public BootstrapperParameter toParameter() {
    return new BootstrapperParameter(bootstrappingCoreTopic, bootstrapTimeoutMs);
  }

  public ByteString getBootstrappingCoreTopic() {
    return bootstrappingCoreTopic;
  }

  public int getBootstrapTimeoutMs() {
    return bootstrapTimeoutMs;
  }
}
