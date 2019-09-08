package de.unistuttgart.isw.sfsc.commonjava.zmq.highlevelinbox;

import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.SubscriptionManager;
import java.util.function.Consumer;

public class HighLevelInbox implements Consumer<byte[][]> {

  private final TopicDistributor topicDistributor = new TopicDistributor();
  private final SubscriptionManager subscriptionManager;

  public HighLevelInbox(SubscriptionManager subscriptionManager) {
    this.subscriptionManager = subscriptionManager;
  }

  public void add(TopicListener topicListener) {
    subscriptionManager.subscribe(topicListener.getTopic());
    topicDistributor.add(topicListener);
  }

  public void remove(TopicListener topicListener) {
    subscriptionManager.unsubscribe(topicListener.getTopic());
    topicDistributor.remove(topicListener);
  }

  @Override
  public void accept(byte[][] bytes) {
    topicDistributor.accept(bytes);
  }
}
