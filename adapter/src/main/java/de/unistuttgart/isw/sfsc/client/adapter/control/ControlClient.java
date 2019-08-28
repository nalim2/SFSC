package de.unistuttgart.isw.sfsc.client.adapter.control;

import static protocol.pubsub.SubProtocol.SubscriptionType.SUBSCRIPTION;
import static protocol.pubsub.SubProtocol.TYPE_AND_TOPIC_FRAME;

import de.unistuttgart.isw.sfsc.client.adapter.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import protocol.pubsub.SubProtocol;
import zmq.pubsubsocketpair.PubSubSocketPair;
import zmq.pubsubsocketpair.SimplePubSubSocketPair;
import zmq.reactor.Reactor;

public class ControlClient implements AutoCloseable {

  private final SimplePubSubSocketPair pubSubSocketPair;
  private final ControlInboxHandler controlInboxHandler;
  private final SubscriptionEventInboxHandler subscriptionEventInboxHandler;

  ControlClient(SimplePubSubSocketPair pubSubSocketPair, ControlInboxHandler controlInboxHandler,
      SubscriptionEventInboxHandler subscriptionEventInboxHandler) {
    this.pubSubSocketPair = pubSubSocketPair;
    this.controlInboxHandler = controlInboxHandler;
    this.subscriptionEventInboxHandler = subscriptionEventInboxHandler;
  }

  public static ControlClient create(Reactor reactor, BootstrapConfiguration configuration, UUID uuid, CountDownLatch ready)
      throws ExecutionException, InterruptedException {
    SimplePubSubSocketPair pubSubSocketPair = SimplePubSubSocketPair.create(reactor);
    ControlInboxHandler controlInboxHandler = ControlInboxHandler.create(pubSubSocketPair);
    SubscriptionEventInboxHandler subscriptionEventInboxHandler = SubscriptionEventInboxHandler.create(pubSubSocketPair, uuid, ready);
    ControlClient controlClient = new ControlClient(pubSubSocketPair, controlInboxHandler, subscriptionEventInboxHandler);
    connect(controlClient.pubSubSocketPair, configuration);
    controlClient.subscribe(uuid);
    return controlClient;
  }

  static void connect(PubSubSocketPair pubSubSocketPair, BootstrapConfiguration configuration) {
    pubSubSocketPair.subscriberSocketConnector().connect(configuration.getCoreHost(), configuration.getCorePort());
  }

  void subscribe(UUID uuid) {
    byte[] subscription = SubProtocol.buildTypeAndTopicFrame(SUBSCRIPTION, serializeUuid(uuid));
    byte[][] message = SubProtocol.newEmptyMessage();
    TYPE_AND_TOPIC_FRAME.put(message, subscription);
    pubSubSocketPair.subscriptionManager().outbox().add(message);
  }

  static byte[] serializeUuid(UUID uuid) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES + Long.BYTES);
    byteBuffer.putLong(uuid.getMostSignificantBits());
    byteBuffer.putLong(uuid.getLeastSignificantBits());
    return byteBuffer.array();
  }

  public void connectPubSocket(WelcomeMessage welcomeMessage) {
    pubSubSocketPair.publisherSocketConnector().connect(welcomeMessage.getHost(), welcomeMessage.getControlSubPort());
  }

  public Future<WelcomeMessage> getWelcomeMessage() {
    return controlInboxHandler.getWelcomeMessageFuture();
  }

  @Override
  public void close() {
    pubSubSocketPair.close();
    controlInboxHandler.close();
    subscriptionEventInboxHandler.close();
  }
}
