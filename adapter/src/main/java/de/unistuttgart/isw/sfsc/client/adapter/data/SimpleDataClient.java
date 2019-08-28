package de.unistuttgart.isw.sfsc.client.adapter.data;

import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.concurrent.ExecutionException;
import zmq.pubsubsocketpair.PubSubSocketPair.Publisher;
import zmq.pubsubsocketpair.PubSubSocketPair.SubscriptionManager;
import zmq.pubsubsocketpair.SimplePubSubSocketPair;
import zmq.reactor.ReactiveSocket.Inbox;
import zmq.reactor.Reactor;

public class SimpleDataClient implements DataClient, AutoCloseable {

  private final SimplePubSubSocketPair pubSubSocketPair;

  SimpleDataClient(SimplePubSubSocketPair pubSubSocketPair) {
    this.pubSubSocketPair = pubSubSocketPair;
  }

  public static SimpleDataClient create(Reactor reactor, WelcomeMessage welcomeMessage) throws ExecutionException, InterruptedException {
    SimplePubSubSocketPair pubSubSocketPair = SimplePubSubSocketPair.create(reactor);
    SimpleDataClient dataClient = new SimpleDataClient(pubSubSocketPair);
    pubSubSocketPair.publisherSocketConnector().connect(welcomeMessage.getHost(), welcomeMessage.getDataSubPort());
    pubSubSocketPair.subscriberSocketConnector().connect(welcomeMessage.getHost(), welcomeMessage.getDataPubPort());
    return dataClient;
  }

  @Override
  public Publisher publisher() {
    return pubSubSocketPair.publisher();
  }

  @Override
  public Inbox dataInbox() {
    return pubSubSocketPair.dataInbox();
  }

  @Override
  public SubscriptionManager subscriptionManager() {
    return pubSubSocketPair.subscriptionManager();
  }

  @Override
  public Inbox subEventInbox() {
    return pubSubSocketPair.subEventInbox();
  }

  @Override
  public void close()  {
    pubSubSocketPair.close();
  }
}
