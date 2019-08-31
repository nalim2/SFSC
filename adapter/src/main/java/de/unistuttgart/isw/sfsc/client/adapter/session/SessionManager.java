package de.unistuttgart.isw.sfsc.client.adapter.session;

import static protocol.pubsub.DataProtocol.PAYLOAD_FRAME;

import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.protocol.control.SessionMessage;
import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zmq.processors.MessageDistributor.TopicListener;

public class SessionManager implements TopicListener, AutoCloseable {

  private static final String SESSION_BASE_TOPIC = "session";
  private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

  private final Pattern pattern;
  private final String topic;
  private final Consumer<WelcomeMessage> welcomeResponseConsumer;

  SessionManager(Consumer<WelcomeMessage> welcomeResponseConsumer, UUID uuid) {
    this.welcomeResponseConsumer = welcomeResponseConsumer;
    topic = SESSION_BASE_TOPIC + "///" + uuid; //todo ///
    pattern = Pattern.compile("\\A" + topic + "\\z");
  }

  public static SessionManager create(Consumer<WelcomeMessage> welcomeResponseConsumer, UUID uuid) {
    return new SessionManager(welcomeResponseConsumer, uuid);
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
          welcomeResponseConsumer.accept(welcomeMessage);
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
  public void close() {
  }
}
