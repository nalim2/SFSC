package de.unistuttgart.isw.sfsc.core.control;

import static protocol.pubsub.DataProtocol.PAYLOAD_FRAME;
import static protocol.pubsub.DataProtocol.TOPIC_FRAME;
import static protocol.pubsub.SubProtocol.TYPE_AND_TOPIC_FRAME;

import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import de.unistuttgart.isw.sfsc.protocol.control.SessionMessage;
import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.pubsub.DataProtocol;
import protocol.pubsub.SubProtocol;
import protocol.pubsub.SubProtocol.SubscriptionType;
import zmq.pubsubsocketpair.PubSubSocketPair;
import zmq.reactor.ReactiveSocket.Inbox;
import zmq.reactor.ReactiveSocket.Outbox;

public class SubscriptionEventInboxHandler implements AutoCloseable {

  private static final Supplier<Integer> threadCounter = new AtomicInteger()::getAndIncrement;
  private static final Logger logger = LoggerFactory.getLogger(SubscriptionEventInboxHandler.class);

  private final ExecutorService daemonExecutor = Executors.newSingleThreadExecutor();
  private final Inbox subInbox;
  private final Outbox subOutbox;
  private final Outbox controlOutbox;
  private final WelcomeMessage welcomeMessage;

  SubscriptionEventInboxHandler(Inbox subInbox, Outbox subOutbox, Outbox controlOutbox, WelcomeMessage welcomeMessage) {
    this.subInbox = subInbox;
    this.subOutbox = subOutbox;
    this.controlOutbox = controlOutbox;
    this.welcomeMessage = welcomeMessage;
  }

  public static SubscriptionEventInboxHandler create(PubSubSocketPair pubSubSocketPair, Configuration<CoreOption> configuration) {
    WelcomeMessage welcomeMessage = createWelcomeMessage(configuration);
    SubscriptionEventInboxHandler subscriptionEventInboxHandler = new SubscriptionEventInboxHandler(pubSubSocketPair.getSubEventInbox(),
        pubSubSocketPair.getSubEventOutbox(), pubSubSocketPair.getDataOutbox(), welcomeMessage);
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
        subOutbox.add(message);
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
    byte[] topicBytes = SubProtocol.getTopic(typeAndTopicFrame);
    switch (subscriptionType) {
      case SUBSCRIPTION:
        logger.info("Subscription to topic {}", new String(topicBytes));
        byte[][] pubMessage = DataProtocol.newEmptyMessage();
        TOPIC_FRAME.put(pubMessage, SubProtocol.getTopic(typeAndTopicFrame));
        PAYLOAD_FRAME.put(pubMessage, SessionMessage.newBuilder().setWelcomeMessage(welcomeMessage).build().toByteArray());
        controlOutbox.add(pubMessage);
        break;
      case UNSUBSCRIPTION:
        logger.info("Unsubscription from topic {}", new String(topicBytes));
        break;
      default:
        logger.warn("Received unsupported subscription type {}", subscriptionType);
    }
  }

  static WelcomeMessage createWelcomeMessage(Configuration<CoreOption> configuration) {
    return WelcomeMessage.newBuilder()
        .setHost(configuration.get(CoreOption.HOST))
        .setControlPubPort(Integer.parseInt(configuration.get(CoreOption.CONTROL_PUB_PORT)))
        .setControlSubPort(Integer.parseInt(configuration.get(CoreOption.CONTROL_SUB_PORT)))
        .setDataPubPort(Integer.parseInt(configuration.get(CoreOption.DATA_PUB_PORT)))
        .setDataSubPort(Integer.parseInt(configuration.get(CoreOption.DATA_SUB_PORT)))
        .build();
  }

  @Override
  public void close() {
    daemonExecutor.shutdownNow();
  }
}
