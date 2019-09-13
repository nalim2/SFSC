package de.unistuttgart.isw.sfsc.client.adapter.raw;

public class BootstrapConfiguration {
  private final String coreHost;
  private final int corePort;

  public BootstrapConfiguration(String coreHost, int corePort) {
    this.coreHost = coreHost;
    this.corePort = corePort;
  }

  public String getCoreHost() {
    return coreHost;
  }

  public int getCorePort() {
    return corePort;
  }

}
