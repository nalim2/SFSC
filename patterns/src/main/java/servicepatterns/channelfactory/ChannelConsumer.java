package servicepatterns.channelfactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.patterns.channelfactory.ChannelFactoryReply;
import de.unistuttgart.isw.sfsc.patterns.channelfactory.ChannelFactoryRequest;
import java.util.function.Consumer;
import java.util.function.Function;
import servicepatterns.Service;
import servicepatterns.ServiceApi;
import servicepatterns.SfscMessage;

public class ChannelConsumer implements Function<SfscMessage, Service> {

  private final ServiceApi serviceApi;
  private final Consumer<SfscMessage> consumer;

  public ChannelConsumer(ServiceApi serviceApi, Consumer<SfscMessage> consumer) {
    this.serviceApi = serviceApi;
    this.consumer = consumer;
  }

  public ByteString getMessage(ByteString payload) {
    return ChannelFactoryRequest.newBuilder().setPayload(payload).build().toByteString();
  }

  @Override
  public Service apply(SfscMessage sfscMessage) {
    if (sfscMessage.hasError()) {
      throw new ChannelFactoryException("received message has error "+ sfscMessage.getError());
    } else {
      try {
        ChannelFactoryReply reply = ChannelFactoryReply.parseFrom(sfscMessage.getPayload());
        return serviceApi.subscriber(reply.getTagsMap(), consumer);
      } catch (InvalidProtocolBufferException e) {
        throw new ChannelFactoryException(e);
      }
    }
  }
}
