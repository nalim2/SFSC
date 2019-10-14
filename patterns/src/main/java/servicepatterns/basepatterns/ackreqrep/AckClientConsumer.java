package servicepatterns.basepatterns.ackreqrep;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import de.unistuttgart.isw.sfsc.patterns.ackreqrep.Reply;
import de.unistuttgart.isw.sfsc.patterns.ackreqrep.RequestOrAcknowledge;
import de.unistuttgart.isw.sfsc.patterns.ackreqrep.RequestOrAcknowledge.Acknowledge;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servicepatterns.CallbackRegistry;

final class AckClientConsumer implements Consumer<ByteString> {

  private static final Logger logger = LoggerFactory.getLogger(AckClientConsumer.class);

  private final CallbackRegistry callbackRegistry;
  private final OutputPublisher publisher;

  AckClientConsumer(CallbackRegistry callbackRegistry, OutputPublisher publisher) {
    this.callbackRegistry = callbackRegistry;
    this.publisher = publisher;
  }

  @Override
  public void accept(ByteString byteString) {
    try {
      Reply reply = Reply.parseFrom(byteString);
      int replyId = reply.getReplyId();
      ByteString replyPayload = reply.getReplyPayload();
      callbackRegistry.performCallback(replyId, replyPayload);
      ByteString acknowledgeTopic = reply.getAcknowledgeTopic();
      int acknowledgeId = reply.getExpectedAcknowledgeId();
      ByteString acknowledge = wrapAcknowledge(acknowledgeId);
      publisher.publish(acknowledgeTopic, acknowledge);
    } catch (InvalidProtocolBufferException e) {
      logger.warn("Received malformed message", e);
    }
  }

  ByteString wrapAcknowledge(int id) {
    Acknowledge acknowledge = Acknowledge.newBuilder()
        .setAcknowledgeId(id)
        .build();

    return RequestOrAcknowledge
        .newBuilder()
        .setAcknowledge(acknowledge)
        .build()
        .toByteString();
  }
}
