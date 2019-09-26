package servicepatterns.pubsub;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import de.unistuttgart.isw.sfsc.patterns.Tags;
import java.util.Map;
import servicepatterns.ServiceImpl;

class PublisherImpl extends ServiceImpl implements Publisher {

  private final OutputPublisher publisher;
  private final byte[] topicCache;

  PublisherImpl(Map<String, ByteString> tags, OutputPublisher publisher, Runnable closer) {
    super(tags, closer);
    this.publisher = publisher;
    topicCache = getTags().get(Tags.OUTPUT_TOPIC.name()).toByteArray();
  }

  @Override
  public void publish(ByteString payload) {
    publisher.publish(topicCache, payload.toByteArray());
  }
}
