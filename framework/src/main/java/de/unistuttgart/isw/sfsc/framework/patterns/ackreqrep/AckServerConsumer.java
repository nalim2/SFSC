package de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.commonjava.registry.CallbackRegistry;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.MaxTimesRepetition;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.OutputPublisher;
import de.unistuttgart.isw.sfsc.framework.messagingpatterns.ackreqrep.Reply;
import de.unistuttgart.isw.sfsc.framework.messagingpatterns.ackreqrep.RequestOrAcknowledge;
import de.unistuttgart.isw.sfsc.framework.messagingpatterns.ackreqrep.RequestOrAcknowledge.Acknowledge;
import de.unistuttgart.isw.sfsc.framework.messagingpatterns.ackreqrep.RequestOrAcknowledge.Request;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import de.unistuttgart.isw.sfsc.framework.types.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AckServerConsumer implements BiConsumer<ByteString, ByteString> {

  private static final Logger logger = LoggerFactory.getLogger(AckServerConsumer.class);

  private final Supplier<Integer> idGenerator = new AtomicInteger()::getAndIncrement;

  private final OutputPublisher publisher;
  private final CallbackRegistry callbackRegistry;
  private final Scheduler scheduler;
  private final ByteString serverTopic;
  private final Function<ByteString, AckServerResult> serverFunction;
  private final int timeoutMs;
  private final int sendRateMs;
  private final int sendMaxTries;

  AckServerConsumer(OutputPublisher publisher, CallbackRegistry callbackRegistry, Scheduler scheduler, Function<ByteString, AckServerResult> serverFunction,
      ByteString serverTopic, int timeoutMs, int sendRateMs, int sendMaxTries) {
    this.publisher = publisher;
    this.callbackRegistry = callbackRegistry;
    this.scheduler = scheduler;
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
          ByteString replyTopic = request.getReplyTopic().getTopic();
          ByteString requestPayload = request.getRequestPayload();
          AckServerResult ackServerResult = serverFunction.apply(requestPayload);
          int acknowledgeId = idGenerator.get();
          Reply wrappedReply = wrapReply(acknowledgeId, serverTopic, replyId, ackServerResult.getResponse());

          MaxTimesRepetition maxTimesRepetition = MaxTimesRepetition.scheduleMaxTimes(
              scheduler,
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

  Reply wrapReply(int acknowledgeId, ByteString acknowledgeTopic, int replyId, Message payload) {
    return Reply
        .newBuilder()
        .setAcknowledgeTopic(Topic.newBuilder().setTopic(acknowledgeTopic).build())
        .setExpectedAcknowledgeId(acknowledgeId)
        .setReplyId(replyId)
        .setReplyPayload(payload.toByteString())
        .build();
  }
}
