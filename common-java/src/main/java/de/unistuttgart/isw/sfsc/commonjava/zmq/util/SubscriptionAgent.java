package de.unistuttgart.isw.sfsc.commonjava.zmq.util;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.data.DataMultiplexer;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.SubscriptionManager;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SubscriptionAgent {

  private final DataMultiplexer dataMultiplexer;
  private final SubscriptionManager subscriptionManager;

  SubscriptionAgent(DataMultiplexer dataMultiplexer, SubscriptionManager subscriptionManager) {
    this.dataMultiplexer = dataMultiplexer;
    this.subscriptionManager = subscriptionManager;
  }

  public static SubscriptionAgent create(PubSubConnection pubSubConnection) {
    DataMultiplexer dataMultiplexer = pubSubConnection.dataMultiplexer();
    SubscriptionManager subscriptionManager = pubSubConnection.subscriptionManager();
    return new SubscriptionAgent(dataMultiplexer, subscriptionManager);
  }

  public Handle addSubscriber(Set<ByteString> topics, Predicate<ByteString> filter, BiConsumer<ByteString, ByteString> messageHandler,
      Executor handlerExecutor) {
    Set<ByteString> topicsCopy = Set.copyOf(topics);
    Handle multiplexHandle = dataMultiplexer.add(filter, (topic, data) -> handlerExecutor.execute(() -> messageHandler.accept(topic, data)));
    Set<Handle> subscriptionHandles = topicsCopy.stream().map(subscriptionManager::subscribe).collect(Collectors.toUnmodifiableSet());
    return () -> {
      subscriptionHandles.forEach(Handle::close);
      multiplexHandle.close();
    };
  }

  public Handle addSubscriber(ByteString topic, Predicate<ByteString> filter, BiConsumer<ByteString, ByteString> messageHandler, Executor executor) {
    return addSubscriber(Set.of(topic), filter, messageHandler, executor);
  }

  public Handle addSubscriber(ByteString topic, BiConsumer<ByteString, ByteString> messageHandler, Executor executor) {
    return addSubscriber(topic, topic::equals, messageHandler, executor);
  }

}
