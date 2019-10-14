package servicepatterns.services.channelfactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.patterns.channelfactory.ChannelFactoryReply;
import de.unistuttgart.isw.sfsc.patterns.channelfactory.ChannelFactoryRequest;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import servicepatterns.api.SfscServiceApi;
import servicepatterns.api.SfscSubscriber;

public class ChannelFactoryClient implements Function<ByteString, SfscSubscriber> {

  private final SfscServiceApi sfscServiceApi;
  private final Consumer<ByteString> consumer;

  public ChannelFactoryClient(SfscServiceApi sfscServiceApi, Consumer<ByteString> consumer) {
    this.sfscServiceApi = sfscServiceApi;
    this.consumer = consumer;
  }

  public static ByteString getMessage(ByteString payload) {
    return ChannelFactoryRequest.newBuilder().setPayload(payload).build().toByteString();
  }

  @Override
  public SfscSubscriber apply(ByteString byteString) {
    try {
      ChannelFactoryReply reply = ChannelFactoryReply.parseFrom(byteString);
      Map<String, ByteString> tags = reply.getTagsMap();
      if (tags != null) {
        return sfscServiceApi.subscriber(tags, consumer);
      } else {
        throw new ChannelFactoryException("Response does not contain publisher information");
      }
    } catch (InvalidProtocolBufferException e) {
      throw new ChannelFactoryException(e);
    }
  }
}
