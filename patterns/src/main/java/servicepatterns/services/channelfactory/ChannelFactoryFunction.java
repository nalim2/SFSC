package servicepatterns.services.channelfactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.commonjava.zmq.subscriptiontracker.SubscriptionTracker;
import de.unistuttgart.isw.sfsc.patterns.channelfactory.ChannelFactoryReply;
import de.unistuttgart.isw.sfsc.patterns.channelfactory.ChannelFactoryRequest;
import de.unistuttgart.isw.sfsc.patterns.tags.PublisherTags;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelFactoryFunction implements Function<ByteString, ByteString> {

  private static final Logger logger = LoggerFactory.getLogger(ChannelFactoryFunction.class);

  private final Function<ByteString, ChannelFactoryResult> channelFactory;
  private final SubscriptionTracker subscriptionTracker;
  private final Executor confirmationExecutor;

  public ChannelFactoryFunction(Function<ByteString, ChannelFactoryResult> channelFactory, SubscriptionTracker subscriptionTracker,
      Executor executor) {
    this.channelFactory = channelFactory;
    this.subscriptionTracker = subscriptionTracker;
    this.confirmationExecutor = executor;
  }

  @Override
  public ByteString apply(ByteString byteString) {
    try {
      ChannelFactoryRequest request = ChannelFactoryRequest.parseFrom(byteString);
      ByteString payload = request.getPayload();
      ChannelFactoryResult channelFactoryResult = channelFactory.apply(payload);

      if (channelFactoryResult != null) {
        Map<String, ByteString> tags = channelFactoryResult.getPublisher().getTags();
        ByteString topic = tags.get(PublisherTags.SFSC_PUBLISHER_OUTPUT_TOPIC.name());
        SubscriptionConfirmer.create(topic, channelFactoryResult.getOnSubscriptionRunnable(), subscriptionTracker, confirmationExecutor);
        return ChannelFactoryReply.newBuilder().putAllTags(tags).build().toByteString();
      } else {
        return ChannelFactoryReply.getDefaultInstance().toByteString();
      }
    } catch (InvalidProtocolBufferException e) {
      logger.warn("received malformed request", e);
      return ChannelFactoryReply.getDefaultInstance().toByteString();
    }
  }
}
