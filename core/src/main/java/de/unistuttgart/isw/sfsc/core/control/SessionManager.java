package de.unistuttgart.isw.sfsc.core.control;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.zmq.comfortinbox.TopicListener;
import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.SubscriptionEventProcessor.SubscriptionListener;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.Publisher;
import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import de.unistuttgart.isw.sfsc.protocol.session.SessionMessage;
import de.unistuttgart.isw.sfsc.protocol.session.WelcomeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SessionManager implements SubscriptionListener, TopicListener {

  private static final String TOPIC = "session";
  private final ByteString TOPIC_BYTE_STRING = ByteString.copyFromUtf8(TOPIC);

  private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
  private final Publisher publisher;
  private final WelcomeMessage welcome;

  SessionManager(Configuration<CoreOption> configuration, Publisher publisher) {
    welcome = createWelcomeMessage(configuration);
    this.publisher = publisher;
  }

  @Override
  public void onSubscription(ByteString topic) {
    logger.info("Received subscription to topic {}", topic.toStringUtf8());
    SessionMessage payload = SessionMessage.newBuilder().setWelcomeMessage(welcome).build();
    publisher.publish(topic, payload);
  }

  @Override
  public void onUnsubscription(ByteString topic) {
    logger.info("Received unsubscription from topic {}", topic.toStringUtf8());
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
  public ByteString getTopic() {
    return TOPIC_BYTE_STRING;
  }

  @Override
  public boolean test(ByteString topic) {
    return false;
  }

  @Override
  public void processMessage(byte[][] bytes) {
    throw new UnsupportedOperationException("not yet implemented");
  }

}
