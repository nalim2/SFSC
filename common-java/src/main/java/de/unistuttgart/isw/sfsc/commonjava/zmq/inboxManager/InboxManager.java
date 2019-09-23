package de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager;

import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.SubscriptionManager;
import java.util.function.Consumer;

public class InboxManager implements Consumer<byte[][]> {

  private final Multiplexer multiplexer = new Multiplexer();
  private final SubscriptionManager subscriptionManager;

  public InboxManager(SubscriptionManager subscriptionManager) {
    this.subscriptionManager = subscriptionManager;
  }

  public void addTopic(TopicListener topicListener) {
    topicListener.getTopics().forEach(subscriptionManager::subscribe);
    multiplexer.add(topicListener);
  }

  public void removeTopic(TopicListener topicListener) {
    topicListener.getTopics().forEach(subscriptionManager::unsubscribe);
    multiplexer.remove(topicListener);
  }

  @Override
  public void accept(byte[][] bytes) {
    multiplexer.accept(bytes);
  }
}
