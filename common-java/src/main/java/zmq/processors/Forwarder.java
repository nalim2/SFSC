package zmq.processors;

import java.util.List;
import java.util.function.Consumer;
import zmq.reactor.ReactiveSocket.Outbox;

public class Forwarder implements Consumer<byte[][]> {

  private final List<Outbox> outboxes;

  public Forwarder(List<Outbox> outboxes) {
    this.outboxes = List.copyOf(outboxes);
  }

  public Forwarder(Outbox outbox) {
    this.outboxes = List.of(outbox);
  }

  @Override
  public void accept(byte[][] message) {
    for (Outbox outbox : outboxes) {
      outbox.add(message);
    }
  }

}
