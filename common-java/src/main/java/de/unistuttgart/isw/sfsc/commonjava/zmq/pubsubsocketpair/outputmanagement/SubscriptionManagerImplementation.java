package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol.SubscriptionType;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Outbox;

public class SubscriptionManagerImplementation implements SubscriptionManager {

  private final Outbox subscriptionOutbox;

  public SubscriptionManagerImplementation(Outbox subscriptionOutbox) {
    this.subscriptionOutbox = subscriptionOutbox;
  }

  @Override
  public Handle subscribe(byte[] topic) {
    sendSubscriptionMessage(SubscriptionType.SUBSCRIPTION, topic);
    return () -> sendSubscriptionMessage(SubscriptionType.UNSUBSCRIPTION, topic);
  }

  @Override
  public Handle subscribe(ByteString topic) {
    return subscribe(topic.toByteArray());
  }

  @Override
  public Handle subscribe(String topic) {
    return subscribe(ByteString.copyFromUtf8(topic));
  }

  void sendSubscriptionMessage(SubscriptionType subscriptionType, byte[] topic) {
    subscriptionOutbox.add(SubProtocol.newMessage(subscriptionType, topic));
  }
}
