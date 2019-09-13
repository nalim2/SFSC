package de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor.Tags;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.BaseService;
import java.util.Map;

class PublisherImpl extends BaseService implements Publisher {

  private final PubSubConnection.Publisher publisher;
  private final byte[] topicCache;

  PublisherImpl(Map<String, ByteString> tags, PubSubConnection.Publisher publisher, Runnable closer) {
    super(tags, closer);
    this.publisher = publisher;
    topicCache = getTags().get(Tags.TOPIC.name()).toByteArray();
  }

  @Override
  public void publish(byte[] payload) {
    publisher.publish(topicCache, payload);
  }
}
