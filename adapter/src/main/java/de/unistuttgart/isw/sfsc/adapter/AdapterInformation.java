package de.unistuttgart.isw.sfsc.adapter;

public final class AdapterInformation {

  private final String coreId;
  private final String adapterId;
  private final String coreHost;
  private final int coreSessionPubPort;
  private final int coreSessionSubPort;
  private final int coreDataPubPort;
  private final int coreDataSubPort;

  public AdapterInformation(String coreId, String adapterId, String coreHost, int coreSessionPubPort, int coreSessionSubPort, int coreDataPubPort,
      int coreDataSubPort) {
    this.coreId = coreId;
    this.adapterId = adapterId;
    this.coreHost = coreHost;
    this.coreSessionPubPort = coreSessionPubPort;
    this.coreSessionSubPort = coreSessionSubPort;
    this.coreDataPubPort = coreDataPubPort;
    this.coreDataSubPort = coreDataSubPort;
  }

  public String getCoreId() {
    return coreId;
  }

  public String getAdapterId() {
    return adapterId;
  }

  public String getCoreHost() {
    return coreHost;
  }

  public int getCoreSessionPubPort() {
    return coreSessionPubPort;
  }

  public int getCoreSessionSubPort() {
    return coreSessionSubPort;
  }

  public int getCoreDataPubPort() {
    return coreDataPubPort;
  }

  public int getCoreDataSubPort() {
    return coreDataSubPort;
  }
}
