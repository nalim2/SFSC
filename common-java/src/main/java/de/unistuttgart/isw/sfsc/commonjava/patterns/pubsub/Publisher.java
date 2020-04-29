package de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.OutputPublisher;

public final class Publisher {

  private final OutputPublisher publisher;

  public Publisher(PubSubConnection pubSubConnection) {
    this.publisher = pubSubConnection.publisher();
  }

  public void publish(byte[] topic, byte[] data) {
    publisher.publish(topic, data);
  }

  public void publish(ByteString topic, byte[] data) {
    publisher.publish(topic, data);
  }

  public void publish(String topic, byte[] data) {
    publisher.publish(topic, data);
  }

  public void publish(byte[] topic, Message data) {
    publisher.publish(topic, data);
  }

  public void publish(ByteString topic, Message data) {
    publisher.publish(topic, data);
  }

  public void publish(String topic, Message data) {
    publisher.publish(topic, data);
  }
}
