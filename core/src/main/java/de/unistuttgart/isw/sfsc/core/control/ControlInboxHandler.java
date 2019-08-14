package de.unistuttgart.isw.sfsc.core.control;

import static protocol.control.ControlProtocol.HEADER_FRAME;
import static protocol.control.ControlProtocol.TOPIC_FRAME;

import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import de.unistuttgart.isw.sfsc.protocol.control.ControlHeader;
import de.unistuttgart.isw.sfsc.protocol.control.ControlMessageType;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zmq.pubsubsocketpair.PubSubSocketPair;
import zmq.reactor.ReactiveSocket.Inbox;
import zmq.reactor.ReactiveSocket.Outbox;

public class ControlInboxHandler implements AutoCloseable {

  private static final Supplier<Integer> threadCounter = new AtomicInteger()::getAndIncrement;
  private static final Logger logger = LoggerFactory.getLogger(ControlInboxHandler.class);

  private final ExecutorService daemonExecutor = Executors.newSingleThreadExecutor();
  private final Executor processingExecutor;
  private final Inbox inbox;
  private final Outbox outbox;
  private final Configuration<CoreOption> configuration;

  public ControlInboxHandler(Inbox inbox, Outbox outbox, Executor processingExecutor,
      Configuration<CoreOption> configuration) {
    this.inbox = inbox;
    this.outbox = outbox;
    this.processingExecutor = processingExecutor;
    this.configuration = configuration;
  }

  public static ControlInboxHandler create(PubSubSocketPair pubSubSocketPair, Executor processingExecutor,
      Configuration<CoreOption> configuration) {
    ControlInboxHandler controlInboxHandler = new ControlInboxHandler(pubSubSocketPair.getDataInbox(), pubSubSocketPair.getDataOutbox(),
        processingExecutor, configuration);
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
        processingExecutor.execute(() -> handleControlMessage(message));
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
      byte[] topic = TOPIC_FRAME.get(controlMessage);
      ControlHeader requestHeader = HEADER_FRAME.get(controlMessage, ControlHeader.parser());
      ControlMessageType type = requestHeader.getType();
      switch (type) {
        default:
          logger.warn("received control message with currently unsupported type {}", type);
      }
    } catch (InvalidProtocolBufferException e) {
      logger.warn("Received malformed Control Message", e);
    }
  }

  @Override
  public void close() {
    daemonExecutor.shutdownNow();
  }
}
