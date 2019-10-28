package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.forwarder;

import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.QueueConnector;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Inbox;
import java.util.List;
import java.util.function.Consumer;

public class ForwardingInbox implements Forwarder, NotThrowingAutoCloseable {

  private final Listeners<Consumer<List<byte[]>>> listeners = new Listeners<>();
  private final QueueConnector<List<byte[]>> queueConnector;

  ForwardingInbox(Inbox inbox) {queueConnector = new QueueConnector<>(inbox::take);}

  public static ForwardingInbox create(Inbox inbox) {
    return new ForwardingInbox(inbox);
  }

  public void start() {
    queueConnector.start(this::accept);
  }

  @Override
  public Handle addListener(Consumer<List<byte[]>> listener) {
    return listeners.add(listener);
  }

  @Override
  public void close() {
    queueConnector.close();
  }

  void accept(List<byte[]> message) {
    listeners.forEach(consumer -> consumer.accept(message));
  }


}
