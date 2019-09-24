package servicepatterns.topicfactoryservice;

import com.google.protobuf.ByteString;
import java.util.Map;
import java.util.Set;
import servicepatterns.Service;
import servicepatterns.pubsub.Publisher;

public class TopicFactoryServiceImpl implements TopicFactoryService {

  private final TopicFactory topicFactory;
  private final Service service;

  public TopicFactoryServiceImpl(TopicFactory topicFactory, Service service) {
    this.topicFactory = topicFactory;
    this.service = service;
  }

  @Override
  public Set<Publisher> getPublishers() {
    return topicFactory.getPublishers();
  }

  @Override
  public Map<String, ByteString> getTags() {
    return service.getTags();
  }

  @Override
  public void close() {
    service.close();
  }
}
