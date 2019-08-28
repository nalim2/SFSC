package zmq.pubsubsocketpair;

import protocol.pubsub.DataProtocol;
import zmq.pubsubsocketpair.PubSubSocketPair.Publisher;
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

  @Override
  public Outbox outbox() {
    return outbox;
  }

}
