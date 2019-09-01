package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair;

import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.Publisher;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.SubscriptionManager;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Connector;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Inbox;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import java.util.concurrent.ExecutionException;
import org.zeromq.SocketType;

public class PubSubSocketPair implements AutoCloseable {

  private final ReactiveSocket publisherSocket;
  private final ReactiveSocket subscriberSocket;
  private final SubscriptionManager subscriptionManager;
  private final Publisher publisher;

  PubSubSocketPair(ReactiveSocket publisherSocket, ReactiveSocket subscriberSocket) {
    this.publisherSocket = publisherSocket;
    this.subscriberSocket = subscriberSocket;
    subscriptionManager = new SimpleSubscriptionManager(subscriberSocket.getOutbox());
    publisher = new SimplePublisher(publisherSocket.getOutbox());
  }

  public static PubSubSocketPair create(Reactor reactor) throws ExecutionException, InterruptedException {
    ReactiveSocket publisher = reactor.createReactiveSocket(SocketType.XPUB, SubProtocol.class);
    ReactiveSocket subscriber = reactor.createReactiveSocket(SocketType.XSUB, DataProtocol.class);
    return new PubSubSocketPair(publisher, subscriber);
  }

  public PubSubConnection connection(){
    return new PubSubConnection() {
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
    };
  }

  public Connector publisherSocketConnector() {
    return publisherSocket.getConnector();
  }

  public Connector subscriberSocketConnector() {
    return subscriberSocket.getConnector();
  }

  @Override
  public void close() {
    subscriberSocket.close();
    publisherSocket.close();
  }
}
