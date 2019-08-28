package de.unistuttgart.isw.sfsc.client.adapter.control;

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

class SessionManager implements TopicListener, AutoCloseable {

  static final String SESSION_TOPIC = "session";

  private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

  private final Pattern pattern;
  private final Consumer<WelcomeMessage> welcomeResponseConsumer;

  SessionManager(Consumer<WelcomeMessage> welcomeResponseConsumer, UUID uuid) {
    this.welcomeResponseConsumer = welcomeResponseConsumer;
    this.pattern = Pattern.compile("\\A" + SESSION_TOPIC + "///" + uuid + "\\z");
  }

  @Override
  public boolean test(byte[] bytes) {
    return pattern.matcher(new String(bytes)).matches();
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

  @Override
  public void close() {
  }
}
