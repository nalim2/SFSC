package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import java.util.List;

public interface ReactiveSocket extends NotThrowingAutoCloseable {

  Inbox getInbox();

  Outbox getOutbox();

  Connector getConnector();

  Settings getSettings();

  interface Inbox {

    List<byte[]> take() throws InterruptedException;
  }

  interface Outbox {

    void add(List<byte[]> output);
  }

  interface Connector {

    String DELIMITER = "://";
    String WILDCARD_HOST = "*";

    static String createAddress(String host, int port) {
      return host + ":" + port;
    }

    static String createWildcardAddress(int port) {
      return createAddress(WILDCARD_HOST, port);
    }

    static String createUri(TransportProtocol protocol, String address) {
      return protocol.asString() + DELIMITER + address;
    }

    void connect(TransportProtocol protocol, String address);

    void disconnect(TransportProtocol protocol, String address);

    void bind(TransportProtocol protocol, String address);

    void unbind(TransportProtocol protocol, String address);
  }

  interface Settings {

    void setXPubVerbose();
  }

  @Override
  void close();
}
