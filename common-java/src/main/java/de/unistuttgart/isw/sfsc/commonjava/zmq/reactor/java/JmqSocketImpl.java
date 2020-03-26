package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.java;

import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.AddressFactory;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket;
import java.util.List;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZMQ.Socket;

class JmqSocketImpl implements ReactiveSocket {

  private static final Logger logger = LoggerFactory.getLogger(JmqSocketImpl.class);
  private final Executor executor;
  private final Socket socket;
  private final Inbox inbox;
  private final Runnable closer;

  JmqSocketImpl(Executor executor, Socket socket, Inbox inbox, Runnable closer) {
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
        executor.execute(() -> {
          int lastElementIndex = output.size() - 1;
          for (int i = 0; i < lastElementIndex; i++) {
            socket.sendMore(output.get(i));
          }
          socket.send(output.get(lastElementIndex));
        });
      }
    };
  }

  @Override
  public Connector getConnector() {
    return new Connector() {
      @Override
      public void connect(String host, int port) {
        executor.execute(() -> {
          String address = AddressFactory.createTcpAddress(host, port);
          socket.connect(address);
          logger.debug("Connected socket {} to {}", socket, address);
        });
      }

      @Override
      public void disconnect(String host, int port) {
        executor.execute(() -> {
          String address = AddressFactory.createTcpAddress(host, port);
          socket.disconnect(address);
          logger.debug("Disconnected socket {} from {}", socket, address);
        });
      }

      @Override
      public void bind(int port) {
        executor.execute(() -> {
          String address = AddressFactory.createTcpWildcardAddress(port);
          socket.bind(address);
          logger.debug("Bound socket {} to {}", socket, address);
        });
      }

      @Override
      public void unbind(int port) {
        executor.execute(() -> {
          String address = AddressFactory.createTcpWildcardAddress(port);
          socket.disconnect(address);
          logger.debug("Unbound socket {} from {}", socket, address);
        });
      }
    };
  }

  @Override
  public Settings getSettings() {
    return new Settings() {

      @Override
      public void setXPubVerbose() {
        executor.execute(() -> {
          if (socket.getSocketType() == SocketType.XPUB) {
            socket.setXpubVerbose(true);
          }
        });
      }
    };
  }

  @Override
  public void close() {
    executor.execute(closer);
  }

}
