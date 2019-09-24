package de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager;

import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.SubscriptionManager;
import java.util.function.Consumer;

public class InboxTopicManagerImpl implements InboxTopicManager, Consumer<byte[][]> {

  private final Multiplexer multiplexer = new Multiplexer();
  private final SubscriptionManager subscriptionManager;

  public InboxTopicManagerImpl(SubscriptionManager subscriptionManager) {
    this.subscriptionManager = subscriptionManager;
  }

  @Override
  public void addTopicListener(TopicListener topicListener) {
    topicListener.getTopics().forEach(subscriptionManager::subscribe);
    multiplexer.add(topicListener);
  }

  @Override
  public void removeTopicListener(TopicListener topicListener) {
    topicListener.getTopics().forEach(subscriptionManager::unsubscribe);
    multiplexer.remove(topicListener);
  }

  @Override
  public void accept(byte[][] bytes) {
    multiplexer.accept(bytes);
  }
}
