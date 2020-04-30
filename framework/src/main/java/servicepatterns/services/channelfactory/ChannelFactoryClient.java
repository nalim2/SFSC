package servicepatterns.services.channelfactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.protocol.channelfactory.ChannelFactoryReply;
import de.unistuttgart.isw.sfsc.framework.protocol.channelfactory.ChannelFactoryRequest;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import servicepatterns.api.SfscServiceApi;
import servicepatterns.api.SfscSubscriber;

public class ChannelFactoryClient {

  private final SfscServiceApi sfscServiceApi;
  private final Consumer<ByteString> consumer;

  public ChannelFactoryClient(SfscServiceApi sfscServiceApi, Consumer<ByteString> consumer) {
    this.sfscServiceApi = sfscServiceApi;
    this.consumer = consumer;
  }

  public ByteString getMessage(ByteString payload) {
    return ChannelFactoryRequest.newBuilder().setPayload(payload).build().toByteString();
  }

  public SfscSubscriber process(ByteString byteString) {
    try {
      ChannelFactoryReply reply = ChannelFactoryReply.parseFrom(byteString);
      SfscServiceDescriptor descriptor = reply.getServiceDescriptor();
      if (descriptor != null) {
        return sfscServiceApi.subscriber(descriptor, consumer);
      } else {
        throw new ChannelFactoryException("Response does not contain publisher information");
      }
    } catch (InvalidProtocolBufferException e) {
      throw new ChannelFactoryException(e);
    }
  }

  public SfscSubscriber handleTimeout() throws TimeoutException {
    throw new TimeoutException();
  }
}
