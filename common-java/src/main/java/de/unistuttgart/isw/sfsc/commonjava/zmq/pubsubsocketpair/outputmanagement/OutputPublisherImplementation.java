package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Outbox;

public class OutputPublisherImplementation implements OutputPublisher {

  private final Outbox outbox;

  public OutputPublisherImplementation(Outbox outbox) {
    this.outbox = outbox;
  }

  @Override
  public void publish(byte[] topic, byte[] data) {
    byte[][] message = DataProtocol.newEmptyMessage();
    DataProtocol.TOPIC_FRAME.put(message, topic);
    DataProtocol.DATA_FRAME.put(message, data);
    outbox.add(message);
  }

  @Override
  public void publish(ByteString topic, byte[] data) {
    publish(topic.toByteArray(), data);
  }

  @Override
  public void publish(String topic, byte[] data) {
    publish(ByteString.copyFromUtf8(topic), data);
  }

  @Override
  public void publish(byte[] topic, ByteString data) {
    publish(topic, data.toByteArray());
  }

  @Override
  public void publish(ByteString topic, ByteString data) {
    publish(topic, data.toByteArray());
  }

  @Override
  public void publish(String topic, ByteString data) {
    publish(topic, data.toByteArray());
  }

  @Override
  public void publish(byte[] topic, Message data) {
    publish(topic, data.toByteArray());
  }

  @Override
  public void publish(ByteString topic, Message data) {
    publish(topic, data.toByteArray());
  }

  @Override
  public void publish(String topic, Message data) {
    publish(topic, data.toByteArray());
  }

  @Override
  public Outbox outbox() {
    return outbox;
  }

}
