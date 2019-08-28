package zmq.pubsubsocketpair;

import java.util.concurrent.ExecutionException;
import org.zeromq.SocketType;
import protocol.pubsub.DataProtocol;
import protocol.pubsub.SubProtocol;
import zmq.reactor.ReactiveSocket;
import zmq.reactor.ReactiveSocket.Connector;
import zmq.reactor.ReactiveSocket.Inbox;
import zmq.reactor.Reactor;

public class SimplePubSubSocketPair implements AutoCloseable, PubSubSocketPair {

  private final ReactiveSocket publisherSocket;
  private final ReactiveSocket subscriberSocket;
  private final SubscriptionManager subscriptionManager;
  private final Publisher publisher;

  SimplePubSubSocketPair(ReactiveSocket publisherSocket, ReactiveSocket subscriberSocket) {
    this.publisherSocket = publisherSocket;
    this.subscriberSocket = subscriberSocket;
    subscriptionManager = new SimpleSubscriptionManager(subscriberSocket.getOutbox());
    publisher = new SimplePublisher(publisherSocket.getOutbox());
  }

  public static SimplePubSubSocketPair create(Reactor reactor) throws ExecutionException, InterruptedException {
    ReactiveSocket publisher = reactor.createReactiveSocket(SocketType.XPUB, SubProtocol.class);
    ReactiveSocket subscriber = reactor.createReactiveSocket(SocketType.XSUB, DataProtocol.class);
    return new SimplePubSubSocketPair(publisher, subscriber);
  }

  @Override
  public Publisher publisher() {
    return publisher;
  }

  @Override
  public Inbox dataInbox() {
    return subscriberSocket.getInbox();
  }

  @Override
  public SubscriptionManager subscriptionManager() {
    return subscriptionManager;
  }

  @Override
  public Inbox subEventInbox() {
    return publisherSocket.getInbox();
  }

  @Override
  public Connector publisherSocketConnector() {
    return publisherSocket.getConnector();
  }

  @Override
  public Connector subscriberSocketConnector() {
    return subscriberSocket.getConnector();
  }

  @Override
  public void close() {
    subscriberSocket.close();
    publisherSocket.close();
  }
}
