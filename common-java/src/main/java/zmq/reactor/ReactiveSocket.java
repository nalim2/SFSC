package zmq.reactor;

public interface ReactiveSocket extends AutoCloseable {

  Inbox getInbox();

  Outbox getOutbox();

  Connector getConnector();

  interface Connector {

    void connect(String host, int port);

    void disconnect(String host, int port);

    void bind(int port);

    void unbind(int port);
  }

  interface Inbox {

    byte[][] take() throws InterruptedException;
  }

  interface Outbox {

    void add(byte[][] output);
  }

}
