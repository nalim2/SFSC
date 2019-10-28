package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor;

import java.util.List;
import java.util.concurrent.Executor;
import org.zeromq.ZMQ.Socket;

class ReactiveSocketImpl implements ReactiveSocket {

  private final Executor executor;
  private final Socket socket;
  private final Inbox inbox;
  private final Runnable closer;

  ReactiveSocketImpl(Executor executor, Socket socket, Inbox inbox, Runnable closer) {
    this.executor = executor;
    this.socket = socket;
    this.inbox = inbox;
    this.closer = closer;
  }

  @Override
  public Inbox getInbox() {
    return inbox;
  }

  @Override
  public Outbox getOutbox() {
    return new Outbox() {
      @Override
      public void add(List<byte[]> output) {
        executor.execute(() -> ReactiveSocketFunctions.write(socket, output));
      }
    };
  }

  @Override
  public Connector getConnector() {
    return new Connector() {
      @Override
      public void connect(String host, int port) {
        executor.execute(() -> ReactiveSocketFunctions.connect(socket, host, port));
      }

      @Override
      public void disconnect(String host, int port) {
        executor.execute(() -> ReactiveSocketFunctions.disconnect(socket, host, port));
      }

      @Override
      public void bind(int port) {
        executor.execute(() -> ReactiveSocketFunctions.bind(socket, port));
      }

      @Override
      public void unbind(int port) {
        executor.execute(() -> ReactiveSocketFunctions.unbind(socket, port));
      }
    };
  }

  @Override
  public Settings getSettings() {
    return new Settings() {

      @Override
      public void setXPubVerbose() {
        executor.execute(() -> ReactiveSocketFunctions.setXPubVerbose(socket));
      }
    };
  }

  @Override
  public void close() {
    executor.execute(closer);
  }

}
