package servicepatterns.topicfactoryservice;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.patterns.SfscError;
import de.unistuttgart.isw.sfsc.patterns.publishergenerator.Reply;
import de.unistuttgart.isw.sfsc.patterns.publishergenerator.Request;
import java.util.Map;
import java.util.function.Function;
import servicepatterns.SfscMessage;

public class TopicConsumer implements Function<SfscMessage, Map<String, ByteString>> {

  public ByteString getMessage(ByteString payload) {
    return Request.newBuilder().setPayload(payload).build().toByteString();
  }

  @Override
  public Map<String, ByteString> apply(SfscMessage sfscMessage) {
    if (sfscMessage.getError() != SfscError.NO_ERROR) {
      throw new TopicFactoryException("received message has error {}" + sfscMessage.getError());
    } else {
      try {
        Reply reply = Reply.parseFrom(sfscMessage.getPayload());
        return reply.getTagsMap();
      } catch (InvalidProtocolBufferException e) {
        throw new TopicFactoryException(e);
      }
    }
  }
}
