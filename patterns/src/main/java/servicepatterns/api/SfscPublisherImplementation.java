package servicepatterns.api;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import java.util.Collections;
import java.util.Map;

final class SfscPublisherImplementation implements SfscPublisher {

  private final ByteString topic;
  private final OutputPublisher publisher;
  private final Map<String, ByteString> tags;
  private final Runnable onClose;

  SfscPublisherImplementation(Map<String, ByteString> tags, ByteString topic, OutputPublisher publisher, Runnable onClose) {
    this.tags = Collections.unmodifiableMap(tags);
    this.topic = topic;
    this.publisher = publisher;
    this.onClose = onClose;
  }

  @Override
  public Map<String, ByteString> getTags() {
    return tags;
  }

  @Override
  public void publish(ByteString payload) {
    publisher.publish(topic, payload);
  }

  @Override
  public void close() {
    onClose.run();
  }
}
