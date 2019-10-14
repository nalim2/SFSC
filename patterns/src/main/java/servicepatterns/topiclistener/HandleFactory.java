package servicepatterns.topiclistener;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager.InboxTopicManager;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class HandleFactory {

  private final InboxTopicManager inboxTopicManager;

  public HandleFactory(InboxTopicManager inboxTopicManager) {
    this.inboxTopicManager = inboxTopicManager;
  }

  public ListenerHandle attach(ByteString topic, Consumer<ByteString> consumer, Executor executor) {
    SingleTopicListener topicListener = new SingleTopicListener(topic, consumer, executor);
    inboxTopicManager.addTopicListener(topicListener);
    return () -> inboxTopicManager.removeTopicListener(topicListener);
  }
}
