package servicepatterns.basepatterns.simplereqrep;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.patterns.simplereqrep.Reply;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servicepatterns.CallbackRegistry;

final class SimpleClientConsumer implements Consumer<ByteString> {

  private static final Logger logger = LoggerFactory.getLogger(SimpleClientConsumer.class);

  private final CallbackRegistry callbackRegistry;

  SimpleClientConsumer(CallbackRegistry callbackRegistry) {
    this.callbackRegistry = callbackRegistry;
  }

  @Override
  public void accept(ByteString byteString) {
    try {
      Reply reply = Reply.parseFrom(byteString);
      int replyId = reply.getReplyId();
      ByteString replyPayload = reply.getReplyPayload();
      callbackRegistry.performCallback(replyId, replyPayload);
    } catch (InvalidProtocolBufferException e) {
      logger.warn("Received malformed message", e);
    }
  }


}
