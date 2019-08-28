package de.unistuttgart.isw.sfsc.client.adapter.control;

import static protocol.pubsub.SubProtocol.TYPE_AND_TOPIC_FRAME;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.pubsub.SubProtocol;
import protocol.pubsub.SubProtocol.SubscriptionType;
import zmq.pubsubsocketpair.PubSubSocketPair;
import zmq.reactor.ReactiveSocket.Inbox;

public class SubscriptionEventInboxHandler implements AutoCloseable {

  private static final Supplier<Integer> threadCounter = new AtomicInteger()::getAndIncrement;
  private static final Logger logger = LoggerFactory.getLogger(SubscriptionEventInboxHandler.class);

  private final ExecutorService daemonExecutor = Executors.newSingleThreadExecutor();
  private final Inbox subInbox;
  private final CountDownLatch ready;
  private final UUID uuid;

  SubscriptionEventInboxHandler(Inbox subInbox, UUID uuid, CountDownLatch ready) {
    this.subInbox = subInbox;
    this.ready = ready;
    this.uuid = uuid;
  }

  public static SubscriptionEventInboxHandler create(PubSubSocketPair pubSubSocketPair, UUID uuid, CountDownLatch ready) {
    SubscriptionEventInboxHandler subscriptionEventInboxHandler = new SubscriptionEventInboxHandler(pubSubSocketPair.subEventInbox(), uuid, ready);
    subscriptionEventInboxHandler.startDaemon();
    return subscriptionEventInboxHandler;
  }

  void startDaemon() {
    daemonExecutor.submit(this::handleDataInboxLoop);
  }

  void handleDataInboxLoop() {
    Thread.currentThread().setName("Control Subscription Event Handling Thread " + threadCounter.get());
    while (!Thread.interrupted()) {
      try {
        byte[][] message = subInbox.take();
        handleSubscriptionMessage(message);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        logger.warn("Unexpected Exception", e);
      }
    }
    logger.debug("{} finished!", Thread.currentThread().getName());
  }

  void handleSubscriptionMessage(byte[][] subscriptionMessage) {
    byte[] typeAndTopicFrame = TYPE_AND_TOPIC_FRAME.get(subscriptionMessage);
    SubscriptionType subscriptionType = SubProtocol.getSubscriptionType(typeAndTopicFrame);
    if (subscriptionType == SubscriptionType.SUBSCRIPTION) {
      ByteBuffer rawTopic = ByteBuffer.wrap(SubProtocol.getTopic(typeAndTopicFrame));
      UUID uuid = new UUID(rawTopic.getLong(), rawTopic.getLong());
      if (uuid.equals(this.uuid)) {
        ready.countDown();
      }
    }
  }


  @Override
  public void close() {
    daemonExecutor.shutdownNow();
  }
}
