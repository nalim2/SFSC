package de.unistuttgart.isw.sfsc.core.control.registry;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.query.QueryReply;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Publisher;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;

final class EventPublisher implements NotThrowingAutoCloseable {

  private final Publisher publisher;
  private final ByteString topic;

  EventPublisher(PubSubConnection pubSubConnection, ByteString topic) {
    publisher = new Publisher(pubSubConnection);
    this.topic = topic;
  }

  void send(QueryReply queryReply) {
    publisher.publish(topic, queryReply);
  }

  @Override
  public void close() {
    publisher.close();
  }
}
