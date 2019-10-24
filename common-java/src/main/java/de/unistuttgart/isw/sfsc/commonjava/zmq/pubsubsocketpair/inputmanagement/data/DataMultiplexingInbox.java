package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.data;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol.DATA_FRAME;
import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol.TOPIC_FRAME;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.QueueConnector;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Inbox;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class DataMultiplexingInbox implements DataMultiplexer, NotThrowingAutoCloseable {

  private final Listeners<TopicListener> listeners = new Listeners<>();
  private final QueueConnector<byte[][]> queueConnector;

  DataMultiplexingInbox(Inbox inbox) {queueConnector = new QueueConnector<>(inbox::take);}

  public static DataMultiplexingInbox create(Inbox inbox) {
    return new DataMultiplexingInbox(inbox);
  }

  public void start() {
    queueConnector.start(this::accept);
  }

  @Override
  public Handle add(Predicate<ByteString> filter, BiConsumer<ByteString, ByteString> handler) {
    return listeners.add(new TopicListener(filter, handler));
  }

  void accept(byte[][] message) {
    ByteString topic = ByteString.copyFrom(TOPIC_FRAME.get(message));
    ByteString data = ByteString.copyFrom(DATA_FRAME.get(message));
    listeners.forEach(topicListener -> {
      if (topicListener.filter.test(topic)) {
        topicListener.messageHandler.accept(topic, data);
      }
    });
  }

  @Override
  public void close() {
    queueConnector.close();
  }

  static class TopicListener {

    private final Predicate<ByteString> filter;
    private final BiConsumer<ByteString, ByteString> messageHandler;

    TopicListener(Predicate<ByteString> filter, BiConsumer<ByteString, ByteString> messageHandler) {
      this.filter = filter;
      this.messageHandler = messageHandler;
    }
  }
}
