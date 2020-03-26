package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor;

public class AddressFactory { //todo make default instead of public

  private static final String WILDCARD_HOST = "*";

  public static String createTcpAddress(String host, int port) {
    return "tcp://" + host + ":" + port;
  }

  public static String createTcpWildcardAddress(int port) {
    return "tcp://" + WILDCARD_HOST + ":" + port;
  }
}
