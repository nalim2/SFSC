package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.Publisher;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Outbox;

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

  public void publish(String topic, byte[] data) {
   publish(ByteString.copyFromUtf8(topic).toByteArray(), data);
  }

  public void publish(byte[] topic, Message data) {
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
