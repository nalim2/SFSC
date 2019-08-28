package zmq.pubsubsocketpair;

import zmq.reactor.ReactiveSocket.Connector;
import zmq.reactor.ReactiveSocket.Inbox;
import zmq.reactor.ReactiveSocket.Outbox;

public interface PubSubSocketPair {

  Publisher publisher();

  Inbox dataInbox();

  SubscriptionManager subscriptionManager();

  Inbox subEventInbox();

  Connector publisherSocketConnector();

  Connector subscriberSocketConnector();

  interface SubscriptionManager {

    void subscribe(byte[] topic);

    void unsubscribe(byte[] topic);

    Outbox outbox();
  }

  interface Publisher {

    void publish(byte[] topic, byte[] data);

    Outbox outbox();

  }
}
