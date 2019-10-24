package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair;

import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Connector;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Inbox;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Outbox;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Settings;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import java.util.concurrent.ExecutionException;
import org.zeromq.SocketType;

public class PubSubSocketPair implements NotThrowingAutoCloseable {

  private final ReactiveSocket publisherSocket;
  private final ReactiveSocket subscriberSocket;

  PubSubSocketPair(ReactiveSocket publisherSocket, ReactiveSocket subscriberSocket) {
    this.publisherSocket = publisherSocket;
    this.subscriberSocket = subscriberSocket;
  }

  public static PubSubSocketPair create(Reactor reactor) throws ExecutionException, InterruptedException {
    ReactiveSocket publisher = reactor.createReactiveSocket(SocketType.XPUB, SubProtocol.class);
    ReactiveSocket subscriber = reactor.createReactiveSocket(SocketType.XSUB, DataProtocol.class);
    return new PubSubSocketPair(publisher, subscriber);
  }

  public Outbox dataOutbox() {
    return publisherSocket.getOutbox();
  }

  public Inbox dataInbox() {
    return subscriberSocket.getInbox();
  }

  public Outbox subscriptionOutbox() {
    return subscriberSocket.getOutbox();
  }

  public Inbox subscriptionInbox() {
    return publisherSocket.getInbox();
  }

  public Connector publisherSocketConnector() {
    return publisherSocket.getConnector();
  }

  public Connector subscriberSocketConnector() {
    return subscriberSocket.getConnector();
  }

  public Settings publisherSettings() {
    return publisherSocket.getSettings();
  }

  public Settings subscriberSettings() {
    return subscriberSocket.getSettings();
  }

  @Override
  public void close() {
    subscriberSocket.close();
    publisherSocket.close();
  }

}
