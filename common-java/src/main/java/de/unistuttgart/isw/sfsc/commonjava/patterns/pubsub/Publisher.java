package de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.OutputPublisher;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public final class Publisher implements NotThrowingAutoCloseable {

  private final Set<Future<Void>> futures = ConcurrentHashMap.newKeySet();

  private final OutputPublisher publisher;

  public Publisher(PubSubConnection pubSubConnection) {
    this.publisher = pubSubConnection.publisher();
  }

  public void send(ByteString targetTopic, ByteString payload) {
    publisher.publish(targetTopic, payload);
  }

  @Override
  public void close() {
    futures.forEach(future -> future.cancel(true));
  }
}
