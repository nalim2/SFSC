package de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager;


public interface InboxTopicManager {

  void addTopicListener(TopicListener topicListener);

  void removeTopicListener(TopicListener topicListener);
}
