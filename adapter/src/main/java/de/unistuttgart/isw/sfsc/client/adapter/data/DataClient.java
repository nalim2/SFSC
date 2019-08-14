package de.unistuttgart.isw.sfsc.client.adapter.data;

import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.concurrent.ExecutionException;
import protocol.pubsub.DataProtocol;
import zmq.pubsubsocketpair.PubSubSocketPair;
import zmq.reactor.ReactiveSocket.Inbox;
import zmq.reactor.ReactiveSocket.Outbox;
import zmq.reactor.Reactor;

public class DataClient implements AutoCloseable{

  private final PubSubSocketPair pubSubSocketPair;

  DataClient(PubSubSocketPair pubSubSocketPair) {
    this.pubSubSocketPair = pubSubSocketPair;
  }

  public static DataClient create(Reactor reactor) throws ExecutionException, InterruptedException {
    PubSubSocketPair pubSubSocketPair = PubSubSocketPair.create(reactor, DataProtocol.class);
    return new DataClient(pubSubSocketPair);
  }

  public void connect(WelcomeMessage welcomeMessage){
    pubSubSocketPair.getPublisherSocketConnector().connect(welcomeMessage.getHost(), welcomeMessage.getDataSubPort());
    pubSubSocketPair.getSubscriberSocketConnector().connect(welcomeMessage.getHost(), welcomeMessage.getDataPubPort());
  }

  public Outbox getDataOutbox() {
    return pubSubSocketPair.getDataOutbox();
  }

  public Inbox getDataInbox() {
    return pubSubSocketPair.getDataInbox();
  }

  public Outbox getSubEventOutbox() {
    return pubSubSocketPair.getSubEventOutbox();
  }

  public Inbox getSubEventInbox() {
    return pubSubSocketPair.getSubEventInbox();
  }

  @Override
  public void close() throws Exception {
    pubSubSocketPair.close();
  }
}
