package de.unistuttgart.isw.sfsc.client.adapter.control;

import static protocol.control.ControlProtocol.HEADER_FRAME;
import static protocol.control.ControlProtocol.PAYLOAD_FRAME;

import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.protocol.control.ControlHeader;
import de.unistuttgart.isw.sfsc.protocol.control.ControlMessageType;
import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zmq.pubsubsocketpair.PubSubSocketPair;
import zmq.reactor.ReactiveSocket.Inbox;

public class ControlInboxHandler implements AutoCloseable {

  private static final Supplier<Integer> threadCounter = new AtomicInteger()::getAndIncrement;
  private static final Logger logger = LoggerFactory.getLogger(ControlInboxHandler.class);

  private final ExecutorService daemonExecutor = Executors.newSingleThreadExecutor();
  private final Inbox inbox;
  private WelcomeMessage welcomeMessage;
  private final FutureTask<WelcomeMessage> welcomeMessageFuture = new FutureTask<>(() -> welcomeMessage);

  public ControlInboxHandler(Inbox inbox) {
    this.inbox = inbox;
  }

  public static ControlInboxHandler create(PubSubSocketPair pubSubSocketPair) {
    ControlInboxHandler controlInboxHandler = new ControlInboxHandler(pubSubSocketPair.getDataInbox());
    controlInboxHandler.startDaemon();
    return controlInboxHandler;
  }

  void startDaemon() {
    daemonExecutor.submit(this::handleDataInboxLoop);
  }

  void handleDataInboxLoop() {
    Thread.currentThread().setName("Control Data Handling Thread " + threadCounter.get());
    while (!Thread.interrupted()) {
      try {
        byte[][] message = inbox.take();
        handleControlMessage(message);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        logger.warn("Unexpected Exception", e);
      }
    }
    logger.debug("{} finished!", Thread.currentThread().getName());
  }

  void handleControlMessage(byte[][] controlMessage) {
    try {
      ControlHeader requestHeader = HEADER_FRAME.get(controlMessage, ControlHeader.parser());
      ControlMessageType type = requestHeader.getType();
      switch (type) {
        case WELCOME:
          welcomeMessage = PAYLOAD_FRAME.get(controlMessage, WelcomeMessage.parser());
          welcomeMessageFuture.run();
          break;
        default:
          logger.warn("received control message with currently unsupported type {}", type);
          break;
      }
    } catch (InvalidProtocolBufferException e) {
      logger.warn("received malformed Control Message", e);
    }
  }

  Future<WelcomeMessage> getWelcomeMessageFuture() {
    return welcomeMessageFuture;
  }

  @Override
  public void close() {
    daemonExecutor.shutdownNow();
  }
}
