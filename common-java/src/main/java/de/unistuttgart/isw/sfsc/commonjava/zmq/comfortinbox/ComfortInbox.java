package de.unistuttgart.isw.sfsc.commonjava.zmq.comfortinbox;

import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.SubscriptionManager;
import java.util.function.Consumer;

public class ComfortInbox implements Consumer<byte[][]> {

  private final Multiplexer multiplexer = new Multiplexer();
  private final SubscriptionManager subscriptionManager;

  public ComfortInbox(SubscriptionManager subscriptionManager) {
    this.subscriptionManager = subscriptionManager;
  }

  public void addTopic(TopicListener topicListener) {
    subscriptionManager.subscribe(topicListener.getTopic());
    multiplexer.add(topicListener);
  }

  public void removeTopic(TopicListener topicListener) {
    subscriptionManager.unsubscribe(topicListener.getTopic());
    multiplexer.remove(topicListener);
  }

  @Override
  public void accept(byte[][] bytes) {
    multiplexer.accept(bytes);
  }
}
