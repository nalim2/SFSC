package servicepatterns.basepatterns.ackreqrep;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.commonjava.registry.CallbackRegistry;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.OutputPublisher;
import de.unistuttgart.isw.sfsc.framework.messagingpatterns.ackreqrep.Reply;
import de.unistuttgart.isw.sfsc.framework.messagingpatterns.ackreqrep.RequestOrAcknowledge;
import de.unistuttgart.isw.sfsc.framework.messagingpatterns.ackreqrep.RequestOrAcknowledge.Acknowledge;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AckClientConsumer implements BiConsumer<ByteString, ByteString> {

  private static final Logger logger = LoggerFactory.getLogger(AckClientConsumer.class);

  private final CallbackRegistry callbackRegistry;
  private final OutputPublisher publisher;

  AckClientConsumer(CallbackRegistry callbackRegistry, OutputPublisher publisher) {
    this.callbackRegistry = callbackRegistry;
    this.publisher = publisher;
  }

  @Override
  public void accept(ByteString ignored, ByteString data) {
    try {
      Reply reply = Reply.parseFrom(data);
      int replyId = reply.getReplyId();
      ByteString replyPayload = reply.getReplyPayload();
      callbackRegistry.performCallback(replyId, replyPayload);
      ByteString acknowledgeTopic = reply.getAcknowledgeTopic();
      int acknowledgeId = reply.getExpectedAcknowledgeId();
      RequestOrAcknowledge acknowledge = wrapAcknowledge(acknowledgeId);
      publisher.publish(acknowledgeTopic, acknowledge);
    } catch (InvalidProtocolBufferException e) {
      logger.warn("Received malformed message", e);
    }
  }

  RequestOrAcknowledge wrapAcknowledge(int id) {
    Acknowledge acknowledge = Acknowledge.newBuilder()
        .setAcknowledgeId(id)
        .build();

    return RequestOrAcknowledge
        .newBuilder()
        .setAcknowledge(acknowledge)
        .build();
  }
}
