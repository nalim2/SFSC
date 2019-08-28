package de.unistuttgart.isw.sfsc.client.adapter.data;

import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.concurrent.ExecutionException;
import zmq.pubsubsocketpair.PubSubSocketPair.Publisher;
import zmq.pubsubsocketpair.PubSubSocketPair.SubscriptionManager;
import zmq.pubsubsocketpair.SimplePubSubSocketPair;
import zmq.reactor.ReactiveSocket.Inbox;
import zmq.reactor.Reactor;

public class DataClient implements AutoCloseable {

  private final SimplePubSubSocketPair pubSubSocketPair;

  DataClient(SimplePubSubSocketPair pubSubSocketPair) {
    this.pubSubSocketPair = pubSubSocketPair;
  }

  public static DataClient create(Reactor reactor) throws ExecutionException, InterruptedException {
    SimplePubSubSocketPair pubSubSocketPair = SimplePubSubSocketPair.create(reactor);
    return new DataClient(pubSubSocketPair);
  }

  public void connect(WelcomeMessage welcomeMessage){
    pubSubSocketPair.publisherSocketConnector().connect(welcomeMessage.getHost(), welcomeMessage.getDataSubPort());
    pubSubSocketPair.subscriberSocketConnector().connect(welcomeMessage.getHost(), welcomeMessage.getDataPubPort());
  }

  public Publisher publisher() {
    return pubSubSocketPair.publisher();
  }

  public Inbox dataInbox() {
    return pubSubSocketPair.dataInbox();
  }

  public SubscriptionManager subscriptionManager() {
    return pubSubSocketPair.subscriptionManager();
  }

  public Inbox subEventInbox() {
    return pubSubSocketPair.subEventInbox();
  }

  @Override
  public void close() {
    pubSubSocketPair.close();
  }
}
