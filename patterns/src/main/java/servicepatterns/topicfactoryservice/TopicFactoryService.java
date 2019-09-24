package servicepatterns.topicfactoryservice;

import java.util.Set;
import servicepatterns.Service;
import servicepatterns.pubsub.Publisher;

public interface TopicFactoryService extends Service {
  Set<Publisher> getPublishers();
}
