package servicepatterns.basepatterns.pubsub;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;

public final class Publisher {

  private final OutputPublisher publisher;

  public Publisher(OutputPublisher publisher) {
    this.publisher = publisher;
  }

  public void send(ByteString targetTopic, ByteString payload) {
    publisher.publish(targetTopic, payload);
  }

}
