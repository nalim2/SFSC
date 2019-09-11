package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol.buildTypeAndTopicFrame;

import com.google.protobuf.ByteString;
import com.google.protobuf.StringValue;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol.SubscriptionType;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.SubscriptionManager;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Outbox;

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
  public void subscribe(ByteString topic) {
    subscribe(topic.toByteArray());
  }

  @Override
  public void subscribe(String topic) {
    subscribe(ByteString.copyFromUtf8(topic));
  }

  @Override
  public void unsubscribe(byte[] topic) {
    sendSubscriptionMessage(SubscriptionType.UNSUBSCRIPTION, topic);
  }

  @Override
  public void unsubscribe(ByteString topic) {
    unsubscribe(topic.toByteArray());
  }

  @Override
  public void unsubscribe(String topic) {
    unsubscribe(ByteString.copyFromUtf8(topic));
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
