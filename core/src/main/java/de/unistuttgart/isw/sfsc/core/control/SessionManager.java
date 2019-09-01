package de.unistuttgart.isw.sfsc.core.control;

import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.MessageDistributor.TopicListener;
import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.SubscriptionEventProcessor.SubscriptionListener;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.Publisher;
import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import de.unistuttgart.isw.sfsc.protocol.session.SessionMessage;
import de.unistuttgart.isw.sfsc.protocol.session.WelcomeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SessionManager implements SubscriptionListener, TopicListener {

  public static final String TOPIC = "session";

  private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
  private final Publisher publisher;
  private final WelcomeMessage welcome;

  SessionManager(Configuration<CoreOption> configuration, Publisher publisher) {
    welcome = createWelcomeMessage(configuration);
    this.publisher = publisher;
  }

  @Override
  public void onSubscription(String topic) {
    logger.info("Subscription to topic {}", topic);
    SessionMessage payload = SessionMessage.newBuilder().setWelcomeMessage(welcome).build();
    publisher.publish(topic, payload);
  }

  @Override
  public void onUnsubscription(String topic) {
    logger.info("Unsubscription from topic {}", topic);
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
  public String getTopic() {
    return TOPIC;
  }

  @Override
  public boolean test(String s) {
    return false;
  }

  @Override
  public void processMessage(byte[][] bytes) {
    throw new UnsupportedOperationException("not yet implemented");
  }

}
