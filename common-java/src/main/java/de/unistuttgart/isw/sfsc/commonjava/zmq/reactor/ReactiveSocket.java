package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;

public interface ReactiveSocket extends NotThrowingAutoCloseable {

  Inbox getInbox();

  Outbox getOutbox();

  Connector getConnector();

  Settings getSettings();

  interface Inbox {

    byte[][] take() throws InterruptedException;
  }

  interface Outbox {

    void add(byte[][] output);
  }

  interface Connector {

    void connect(String host, int port);

    void disconnect(String host, int port);

    void bind(int port);

    void unbind(int port);
  }

  interface Settings {

    void setXPubVerbose();
  }

  @Override
  void close();
}
