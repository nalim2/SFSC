package de.unistuttgart.isw.sfsc.client.adapter.raw.control.session;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol.PAYLOAD_FRAME;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.client.adapter.raw.control.registry.AdapterRegistryClient;
import de.unistuttgart.isw.sfsc.commonjava.util.ConsumerFuture;
import de.unistuttgart.isw.sfsc.protocol.session.SessionMessage;
import de.unistuttgart.isw.sfsc.protocol.session.WelcomeMessage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleSessionManager implements SessionManager {

  private static final Logger logger = LoggerFactory.getLogger(SimpleSessionManager.class);

  private static final Set<String> TOPICS = Set.of(SessionManager.TOPIC, AdapterRegistryClient.TOPIC);

  private final Set<String> missing = new HashSet<>(TOPICS);
  private final ConsumerFuture<WelcomeMessage, WelcomeMessage> welcomeMessageConsumer = new ConsumerFuture<>(x -> x);
  private final FutureTask<Void> ready = new FutureTask<>(() -> null);
  private final ByteString topic;

  SimpleSessionManager(String name) {
    topic = ByteString.copyFromUtf8(TOPIC + "://" + name);
  }

  public static SimpleSessionManager create(String name) {
    return new SimpleSessionManager(name);
  }

  @Override
  public Set<ByteString> getTopics() {
    return Set.of(topic);
  }

  @Override
  public boolean test(ByteString topic) {
    return topic.equals(this.topic);
  }

  @Override
  public void processMessage(byte[][] controlMessage) {
    try {
      SessionMessage request = PAYLOAD_FRAME.get(controlMessage, SessionMessage.parser());
      switch (request.getPayloadCase()) {
        case WELCOME_MESSAGE: {
          WelcomeMessage welcomeMessage = request.getWelcomeMessage();
          welcomeMessageConsumer.accept(welcomeMessage);
          break;
        }
        default: {
          logger.warn("received control message with unsupported type {}", request.getPayloadCase());
          break;
        }
      }
    } catch (InvalidProtocolBufferException e) {
      logger.warn("received malformed Control Message", e);
    }
  }

  @Override
  public Future<WelcomeMessage> getWelcomeMessage() {
    return welcomeMessageConsumer;
  }

  @Override
  public void awaitSessionReady() throws ExecutionException, InterruptedException {
    ready.get();
  }

  @Override
  public void onSubscription(ByteString topicByteString) {
    String topic = topicByteString.toStringUtf8();
    logger.debug("Received subscription to topic {}", topic);
    missing.remove(topic);
    if (missing.isEmpty()) {
      ready.run();
    }
  }

  @Override
  public void onUnsubscription(ByteString topic) {
    logger.debug("Received unsubscription from topic {}", topic.toStringUtf8());
  }

  @Override
  public void close() {
  }
}
