package zmq.pubsubsocketpair;

import static protocol.pubsub.SubProtocol.buildTypeAndTopicFrame;

import com.google.protobuf.StringValue;
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
  public void subscribe(StringValue topic) {
    subscribe(topic.toByteArray());
  }

  @Override
  public void subscribe(String topic) {
    subscribe(StringValue.newBuilder().setValue(topic).build());
  }

  @Override
  public void unsubscribe(byte[] topic) {
    sendSubscriptionMessage(SubscriptionType.UNSUBSCRIPTION, topic);
  }

  @Override
  public void unsubscribe(StringValue topic) {
    unsubscribe(topic.toByteArray());
  }

  @Override
  public void unsubscribe(String topic) {
    unsubscribe(StringValue.newBuilder().setValue(topic).build());
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
