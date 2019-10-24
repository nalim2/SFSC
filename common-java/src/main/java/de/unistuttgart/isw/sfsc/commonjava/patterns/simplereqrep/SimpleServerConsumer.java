package de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.OutputPublisher;
import de.unistuttgart.isw.sfsc.messagingpatterns.reqrep.Reply;
import de.unistuttgart.isw.sfsc.messagingpatterns.reqrep.Request;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SimpleServerConsumer implements BiConsumer<ByteString, ByteString> {

  private static final Logger logger = LoggerFactory.getLogger(SimpleServerConsumer.class);

  private final Function<ByteString, ByteString> serverFunction;
  private final OutputPublisher publisher;

  SimpleServerConsumer(OutputPublisher publisher, Function<ByteString, ByteString> serverFunction) {
    this.publisher = publisher;
    this.serverFunction = serverFunction;
  }

  @Override
  public void accept(ByteString ignored, ByteString data) {
    try {
      Request request = Request.parseFrom(data);
      int replyId = request.getExpectedReplyId();
      ByteString requestPayload = request.getRequestPayload();
      ByteString replyTopic = request.getReplyTopic();
      ByteString replyPayload = serverFunction.apply(requestPayload);
      ByteString wrappedReply = wrapReply(replyId, replyPayload);
      publisher.publish(replyTopic, wrappedReply);
    } catch (InvalidProtocolBufferException e) {
      logger.warn("Received malformed message", e);
    }
  }

  ByteString wrapReply(int id, ByteString payload) {
    return Reply
        .newBuilder()
        .setReplyId(id)
        .setReplyPayload(payload)
        .build()
        .toByteString();
  }
}

