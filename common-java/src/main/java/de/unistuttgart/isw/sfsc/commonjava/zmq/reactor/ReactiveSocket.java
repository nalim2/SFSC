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
