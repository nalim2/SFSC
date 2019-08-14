package zmq.pubsubsocketpair;

import java.util.concurrent.ExecutionException;
import org.zeromq.SocketType;
import protocol.pubsub.SubProtocol;
import zmq.reactor.ReactiveSocket;
import zmq.reactor.ReactiveSocket.Connector;
import zmq.reactor.ReactiveSocket.Inbox;
import zmq.reactor.ReactiveSocket.Outbox;
import zmq.reactor.Reactor;

public class PubSubSocketPair implements AutoCloseable {

  private final ReactiveSocket publisher;
  private final ReactiveSocket subscriber;

  PubSubSocketPair(ReactiveSocket  publisher, ReactiveSocket subscriber) {
    this.publisher = publisher;
    this.subscriber = subscriber;
  }

  public static <DataProtocolT extends Enum<DataProtocolT>> PubSubSocketPair create(Reactor reactor, Class<DataProtocolT> clazz)
      throws ExecutionException, InterruptedException {
    ReactiveSocket publisher = reactor.createReactiveSocket(SocketType.XPUB, SubProtocol.class);
    ReactiveSocket subscriber = reactor.createReactiveSocket(SocketType.XSUB, clazz);
    return new PubSubSocketPair(publisher, subscriber);
  }

  public Outbox getDataOutbox() {
    return publisher.getOutbox();
  }

  public Inbox getDataInbox() {
    return subscriber.getInbox();
  }

  public Outbox getSubEventOutbox() {
    return subscriber.getOutbox();
  }

  public Inbox getSubEventInbox() {
    return publisher.getInbox();
  }

  public Connector getPublisherSocketConnector() {
    return publisher.getConnector();
  }

  public Connector getSubscriberSocketConnector() {
    return subscriber.getConnector();
  }

  @Override
  public void close() throws Exception {
    subscriber.close();
    publisher.close();
  }
}
