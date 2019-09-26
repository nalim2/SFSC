package servicepatterns.channelfactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.patterns.SfscError;
import de.unistuttgart.isw.sfsc.patterns.channelfactory.ChannelFactoryReply;
import de.unistuttgart.isw.sfsc.patterns.channelfactory.ChannelFactoryRequest;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servicepatterns.SfscMessage;
import servicepatterns.SfscMessageImpl;
import servicepatterns.pubsub.Publisher;

public class ChannelFactoryFunction implements Function<SfscMessage, ByteString> {

  private static final Logger logger = LoggerFactory.getLogger(ChannelFactoryFunction.class);

  private final Function<SfscMessage, Publisher> channelFactory;

  public ChannelFactoryFunction(Function<SfscMessage, Publisher> channelFactory) {
    this.channelFactory = channelFactory;
  }

  @Override
  public ByteString apply(SfscMessage sfscMessage) {
    SfscMessage passedRequest;
    if (sfscMessage.hasError()) {
      passedRequest = sfscMessage;
    } else {
      try {
        ChannelFactoryRequest request = ChannelFactoryRequest.parseFrom(sfscMessage.getPayload());
        passedRequest = new SfscMessageImpl(SfscError.NO_ERROR, request.getPayload());
      } catch (InvalidProtocolBufferException e) {
        logger.warn("received malformed request", e);
        passedRequest = new SfscMessageImpl(SfscError.MALFORMED, null);
      }
    }

    Publisher publisher = channelFactory.apply(passedRequest);

    if (publisher != null) {
      Map<String, ByteString> tags = publisher.getTags();
      return ChannelFactoryReply.newBuilder().putAllTags(tags).build().toByteString();
    } else {
      return ChannelFactoryReply.getDefaultInstance().toByteString();
    }
  }

}
