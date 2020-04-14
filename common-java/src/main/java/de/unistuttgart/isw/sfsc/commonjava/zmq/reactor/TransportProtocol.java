package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor;

public enum TransportProtocol {
  TCP("tcp"),
  INPROC("inproc"),
  IPC("ipc");

  private final String asString;

  TransportProtocol(String asString) {
    this.asString = asString;
  }

  public String asString() {
    return asString;
  }
}
