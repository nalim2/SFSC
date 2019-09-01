package zmq.pubsubsocketpair;

import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import protocol.pubsub.DataProtocol;
import zmq.pubsubsocketpair.PubSubConnection.Publisher;
import zmq.reactor.ReactiveSocket.Outbox;

public class SimplePublisher implements Publisher {

  private final Outbox outbox;

  SimplePublisher(Outbox outbox) {
    this.outbox = outbox;
  }

  public void publish(byte[] topic, byte[] data) {
    byte[][] message = DataProtocol.newEmptyMessage();
    DataProtocol.TOPIC_FRAME.put(message, topic);
    DataProtocol.PAYLOAD_FRAME.put(message, data);
    outbox.add(message);
  }

  public void publish(StringValue topic, byte[] data) {
    publish(topic.toByteArray(), data);
  }

  public void publish(String topic, byte[] data) {
   publish(StringValue.newBuilder().setValue(topic).build(), data);
  }

  public void publish(byte[] topic, Message data) {
    publish(topic, data.toByteArray());
  }

  public void publish(StringValue topic, Message data) {
    publish(topic, data.toByteArray());
  }

  public void publish(String topic, Message data) {
    publish(topic, data.toByteArray());
  }

  @Override
  public Outbox outbox() {
    return outbox;
  }

}
