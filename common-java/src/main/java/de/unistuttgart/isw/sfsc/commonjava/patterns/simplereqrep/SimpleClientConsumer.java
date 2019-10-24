package de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.commonjava.registry.CallbackRegistry;
import de.unistuttgart.isw.sfsc.messagingpatterns.reqrep.Reply;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SimpleClientConsumer implements BiConsumer<ByteString, ByteString> {

  private static final Logger logger = LoggerFactory.getLogger(SimpleClientConsumer.class);

  private final CallbackRegistry callbackRegistry;

  SimpleClientConsumer(CallbackRegistry callbackRegistry) {
    this.callbackRegistry = callbackRegistry;
  }

  @Override
  public void accept(ByteString ignored, ByteString data) {
    try {
      Reply reply = Reply.parseFrom(data);
      int replyId = reply.getReplyId();
      ByteString replyPayload = reply.getReplyPayload();
      callbackRegistry.performCallback(replyId, replyPayload);
    } catch (InvalidProtocolBufferException e) {
      logger.warn("Received malformed message", e);
    }
  }


}
