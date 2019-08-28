package zmq.pubsubsocketpair;

import static protocol.pubsub.SubProtocol.buildTypeAndTopicFrame;

import protocol.pubsub.SubProtocol;
import protocol.pubsub.SubProtocol.SubscriptionType;
import zmq.pubsubsocketpair.PubSubSocketPair.SubscriptionManager;
import zmq.reactor.ReactiveSocket.Outbox;

class SimpleSubscriptionManager implements SubscriptionManager {

  private final Outbox subscriptionOutbox;

  SimpleSubscriptionManager(Outbox subscriptionOutbox) {
    this.subscriptionOutbox = subscriptionOutbox;
  }

  @Override
  public void subscribe(byte[] topic) {
    sendSubscriptionMessage(SubscriptionType.SUBSCRIPTION, topic);
  }

  @Override
  public void unsubscribe(byte[] topic) {
    sendSubscriptionMessage(SubscriptionType.UNSUBSCRIPTION, topic);
  }

  @Override
  public Outbox outbox() {
    return subscriptionOutbox;
  }

  void sendSubscriptionMessage(SubscriptionType subscriptionType, byte[] topic) {
    byte[][] message = SubProtocol.newEmptyMessage();
    SubProtocol.TYPE_AND_TOPIC_FRAME.put(message, buildTypeAndTopicFrame(subscriptionType, topic));
    subscriptionOutbox.add(message);
  }
}
