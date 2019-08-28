package de.unistuttgart.isw.sfsc.client.adapter.data;

import zmq.pubsubsocketpair.PubSubSocketPair.Publisher;
import zmq.pubsubsocketpair.PubSubSocketPair.SubscriptionManager;
import zmq.reactor.ReactiveSocket.Inbox;

public interface DataClient {

  Publisher publisher();

  Inbox dataInbox();

  SubscriptionManager subscriptionManager();

  Inbox subEventInbox();

}
