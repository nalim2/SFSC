package de.unistuttgart.isw.sfsc.client.adapter.control.session;

import static de.unistuttgart.isw.sfsc.client.adapter.control.registry.SimpleRegistryClient.REGISTRY_BASE_TOPIC;
import static protocol.pubsub.DataProtocol.PAYLOAD_FRAME;

import com.google.protobuf.InvalidProtocolBufferException;
import consumerfuture.ConsumerFuture;
import de.unistuttgart.isw.sfsc.protocol.control.SessionMessage;
import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleSessionManager implements SessionManager {

  private static final Logger logger = LoggerFactory.getLogger(SimpleSessionManager.class);

  private static final Set<String> TOPICS = Set.of(TOPIC, REGISTRY_BASE_TOPIC);

  private final Set<String> missing = new HashSet<>(TOPICS);
  private final ConsumerFuture<WelcomeMessage, WelcomeMessage> welcomeMessageConsumer = new ConsumerFuture<>(x -> x);
  private final FutureTask<Void> ready = new FutureTask<>(() -> null);
  private final Pattern pattern;
  private final String topic;


  SimpleSessionManager(UUID uuid) {
    topic = TOPIC + "///" + uuid; //todo ///
    pattern = Pattern.compile("\\A" + topic + "\\z");
  }

  public static SimpleSessionManager create(UUID uuid) {
    return new SimpleSessionManager(uuid);
  }

  @Override
  public boolean test(String topic) {
    return pattern.matcher(topic).matches();
  }

  @Override
  public void accept(byte[][] controlMessage) {
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

  public String getTopic() {
    return topic;
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
  public void onSubscription(String topic) {
    missing.remove(topic);
    if (missing.isEmpty()) {
      ready.run();
    }
  }

  @Override
  public void onUnsubscription(String topic) {
  }

  @Override
  public void close() {
  }
}
