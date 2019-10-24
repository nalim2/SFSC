package de.unistuttgart.isw.sfsc.adapter.control.session;

public final class WelcomeInformation {

  private final String coreId;
  private final String adapterId;
  private final int coreDataPubPort;
  private final int coreDataSubPort;

  WelcomeInformation(String coreId, String adapterId, int coreDataPubPort, int coreDataSubPort) {
    this.coreId = coreId;
    this.adapterId = adapterId;
    this.coreDataPubPort = coreDataPubPort;
    this.coreDataSubPort = coreDataSubPort;
  }

  public String getCoreId() {
    return coreId;
  }

  public String getAdapterId() {
    return adapterId;
  }

  public int getCoreDataPubPort() {
    return coreDataPubPort;
  }

  public int getCoreDataSubPort() {
    return coreDataSubPort;
  }
}
