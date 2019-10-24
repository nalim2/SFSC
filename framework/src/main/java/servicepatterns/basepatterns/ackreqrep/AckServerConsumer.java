package servicepatterns.basepatterns.ackreqrep;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.commonjava.registry.CallbackRegistry;
import de.unistuttgart.isw.sfsc.commonjava.util.MaxTimesRepetition;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.OutputPublisher;
import de.unistuttgart.isw.sfsc.framework.messagingpatterns.ackreqrep.Reply;
import de.unistuttgart.isw.sfsc.framework.messagingpatterns.ackreqrep.RequestOrAcknowledge;
import de.unistuttgart.isw.sfsc.framework.messagingpatterns.ackreqrep.RequestOrAcknowledge.Acknowledge;
import de.unistuttgart.isw.sfsc.framework.messagingpatterns.ackreqrep.RequestOrAcknowledge.Request;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AckServerConsumer implements BiConsumer<ByteString, ByteString> {

  private static final Logger logger = LoggerFactory.getLogger(AckServerConsumer.class);

  private final Supplier<Integer> idGenerator = new AtomicInteger()::getAndIncrement;

  private final OutputPublisher publisher;
  private final CallbackRegistry callbackRegistry;
  private final ScheduledExecutorService scheduledExecutorService;
  private final ByteString serverTopic;
  private final Function<ByteString, AckServerResult> serverFunction;
  private final int timeoutMs;
  private final int sendRateMs;
  private final int sendMaxTries;

  AckServerConsumer(OutputPublisher publisher, CallbackRegistry callbackRegistry, ScheduledExecutorService scheduledExecutorService, Function<ByteString, AckServerResult> serverFunction,
      ByteString serverTopic, int timeoutMs, int sendRateMs, int sendMaxTries) {
    this.publisher = publisher;
    this.callbackRegistry = callbackRegistry;
    this.scheduledExecutorService = scheduledExecutorService;
    this.serverTopic = serverTopic;
    this.serverFunction = serverFunction;
    this.timeoutMs = timeoutMs;
    this.sendRateMs = sendRateMs;
    this.sendMaxTries = sendMaxTries;
  }

  @Override
  public void accept(ByteString ignored, ByteString data) {
    try {
      RequestOrAcknowledge requestOrAcknowledge = RequestOrAcknowledge.parseFrom(data);
      switch (requestOrAcknowledge.getRequestOrAcknowledgeCase()) {
        case REQUEST: {
          Request request = requestOrAcknowledge.getRequest();
          int replyId = request.getExpectedReplyId();
          ByteString replyTopic = request.getReplyTopic();
          ByteString requestPayload = request.getRequestPayload();
          AckServerResult ackServerResult = serverFunction.apply(requestPayload);
          int acknowledgeId = idGenerator.get();
          ByteString wrappedReply = wrapReply(acknowledgeId, serverTopic, replyId, ackServerResult.getResponse());

          MaxTimesRepetition maxTimesRepetition = MaxTimesRepetition.scheduleMaxTimes(
              scheduledExecutorService,
              () -> publisher.publish(replyTopic, wrappedReply),
              sendRateMs,
              sendMaxTries
          );

          callbackRegistry.addCallback(
              acknowledgeId,
              ignored2 -> {
                maxTimesRepetition.close();
                ackServerResult.getOnDeliverySuccess().run();
              },
              timeoutMs,
              ackServerResult.getOnDeliveryFail()
          );

          break;
        }
        case ACKNOWLEDGE: {
          Acknowledge acknowledge = requestOrAcknowledge.getAcknowledge();
          int acknowledgeId = acknowledge.getAcknowledgeId();
          callbackRegistry.performCallback(acknowledgeId, null);
          break;
        }
        default: {
          logger.warn("Received unsupported message type {}", requestOrAcknowledge.getRequestOrAcknowledgeCase());
          break;
        }
      }
    } catch (InvalidProtocolBufferException e) {
      logger.warn("Received malformed message", e);
    }
  }

  ByteString wrapReply(int acknowledgeId, ByteString acknowledgeTopic, int replyId, ByteString payload) {
    return Reply
        .newBuilder()
        .setAcknowledgeTopic(acknowledgeTopic)
        .setExpectedAcknowledgeId(acknowledgeId)
        .setReplyId(replyId)
        .setReplyPayload(payload)
        .build()
        .toByteString();
  }
}
