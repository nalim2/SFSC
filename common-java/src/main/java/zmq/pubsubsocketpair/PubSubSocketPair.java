package zmq.pubsubsocketpair;

import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
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

    void subscribe(StringValue topic);

    void subscribe(String topic);

    void unsubscribe(byte[] topic);

    void unsubscribe(StringValue topic);

    void unsubscribe(String topic);

    Outbox outbox();
  }

  interface Publisher {

    void publish(byte[] topic, byte[] data);

    void publish(StringValue topic, byte[] data);

    void publish(String topic, byte[] data);

    void publish(byte[] topic, Message data);

    void publish(StringValue topic, Message data);

    void publish(String topic, Message data);

    Outbox outbox();

  }
}
