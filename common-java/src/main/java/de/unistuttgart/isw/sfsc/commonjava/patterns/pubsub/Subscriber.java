package de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.util.SubscriptionAgent;
import java.util.function.Consumer;

public final class Subscriber implements NotThrowingAutoCloseable {

  private final Handle handle;

  public Subscriber(PubSubConnection pubSubConnection, Consumer<ByteString> subscriberFunction, ByteString subscriberTopic, Scheduler scheduler) {
    handle = SubscriptionAgent.create(pubSubConnection).addSubscriber(subscriberTopic, (ignored, data) -> subscriberFunction.accept(data), scheduler);
  }

  @Override
  public void close() {
    handle.close();
  }
}
