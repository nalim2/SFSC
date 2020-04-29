package de.unistuttgart.isw.sfsc.adapter.data;

import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.TransportProtocol;

public class DataParameter {

  private final TransportProtocol transportProtocol;
  private final String pubAddress;
  private final String subAddress;

  public DataParameter(TransportProtocol transportProtocol, String pubAddress, String subAddress) {
    this.transportProtocol = transportProtocol;
    this.pubAddress = pubAddress;
    this.subAddress = subAddress;
  }

  public TransportProtocol getTransportProtocol() {
    return transportProtocol;
  }

  public String getPubAddress() {
    return pubAddress;
  }

  public String getSubAddress() {
    return subAddress;
  }
}
