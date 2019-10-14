package servicepatterns.basepatterns.simplereqrep;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import de.unistuttgart.isw.sfsc.patterns.simplereqrep.Reply;
import de.unistuttgart.isw.sfsc.patterns.simplereqrep.Request;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SimpleServerConsumer implements Consumer<ByteString> {

  private static final Logger logger = LoggerFactory.getLogger(SimpleServerConsumer.class);

  private final Function<ByteString, ByteString> serverFunction;
  private final OutputPublisher publisher;

  SimpleServerConsumer(OutputPublisher publisher, Function<ByteString, ByteString> serverFunction) {
    this.publisher = publisher;
    this.serverFunction = serverFunction;
  }

  @Override
  public void accept(ByteString byteString) {
    try {
      Request request = Request.parseFrom(byteString);
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

