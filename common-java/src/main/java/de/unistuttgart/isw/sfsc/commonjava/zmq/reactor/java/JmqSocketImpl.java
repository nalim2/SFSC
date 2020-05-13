package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.java;

import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.TransportProtocol;
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
      public void connect(TransportProtocol protocol, String address) {
        executor.execute(() -> {
          String uri = Connector.createUri(protocol, address);
          socket.connect(uri);
          logger.debug("Connected socket {} to {}", socket, uri);
        });
      }

      @Override
      public void disconnect(TransportProtocol protocol, String address) {
        executor.execute(() -> {
          String uri = Connector.createUri(protocol, address);
          socket.disconnect(uri);
          logger.debug("Disconnected socket {} from {}", socket, uri);
        });
      }

      @Override
      public void bind(TransportProtocol protocol, String address) {
        executor.execute(() -> {
          String uri = Connector.createUri(protocol, address);
          socket.bind(uri);
          logger.debug("Bound socket {} to {}", socket, uri);
        });
      }

      @Override
      public void unbind(TransportProtocol protocol, String address) {
        executor.execute(() -> {
          String uri = Connector.createUri(protocol, address);
          socket.disconnect(uri);
          logger.debug("Unbound socket {} from {}", socket, uri);
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
