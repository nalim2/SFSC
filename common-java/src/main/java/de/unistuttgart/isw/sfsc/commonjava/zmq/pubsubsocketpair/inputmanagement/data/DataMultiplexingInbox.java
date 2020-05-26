package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.data;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.PubProtocol;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.QueueConnector;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Inbox;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataMultiplexingInbox implements DataMultiplexer, NotThrowingAutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(DataMultiplexingInbox.class);
  private final Listeners<TopicListener> listeners = new Listeners<>();
  private final QueueConnector<List<byte[]>> queueConnector;

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

  void accept(List<byte[]> message) {
    if (!PubProtocol.isValid(message)) {
      logger.warn("Received invalid data message");
    } else {
      ByteString topic = ByteString.copyFrom(PubProtocol.getTopic(message));
      ByteString data = ByteString.copyFrom(PubProtocol.getData(message));
      listeners.forEach(topicListener -> {
        if (topicListener.filter.test(topic)) {
          topicListener.messageHandler.accept(topic, data);
        }
      });
    }
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
