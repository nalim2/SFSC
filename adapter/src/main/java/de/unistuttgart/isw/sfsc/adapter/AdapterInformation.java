package de.unistuttgart.isw.sfsc.adapter;

import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.TransportProtocol;

public final class AdapterInformation {

  private final String coreId;
  private final String adapterId;
  private final TransportProtocol transportProtocol;

  public AdapterInformation(String coreId, String adapterId, TransportProtocol transportProtocol) {
    this.coreId = coreId;
    this.adapterId = adapterId;
    this.transportProtocol = transportProtocol;
  }

  public String getCoreId() {
    return coreId;
  }

  public String getAdapterId() {
    return adapterId;
  }

  public TransportProtocol getTransportProtocol() {
    return transportProtocol;
  }
}
