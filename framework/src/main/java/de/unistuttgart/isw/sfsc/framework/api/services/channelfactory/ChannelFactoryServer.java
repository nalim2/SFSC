package de.unistuttgart.isw.sfsc.framework.api.services.channelfactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep.AckServerResult;
import de.unistuttgart.isw.sfsc.framework.protocol.channelfactory.ChannelFactoryReply;
import de.unistuttgart.isw.sfsc.framework.protocol.channelfactory.ChannelFactoryRequest;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelFactoryServer implements Function<ByteString, AckServerResult> {

  private static final Logger logger = LoggerFactory.getLogger(ChannelFactoryServer.class);

  private final Function<ByteString, ChannelFactoryResult> channelFactory;

  public ChannelFactoryServer(Function<ByteString, ChannelFactoryResult> channelFactory) {
    this.channelFactory = channelFactory;
  }

  @Override
  public AckServerResult apply(ByteString byteString) {
    try {
      ChannelFactoryRequest request = ChannelFactoryRequest.parseFrom(byteString);
      ByteString payload = request.getPayload();
      ChannelFactoryResult result = channelFactory.apply(payload);
      SfscServiceDescriptor descriptor = result.getPublisher().getDescriptor();
      Message response = ChannelFactoryReply.newBuilder().setServiceDescriptor(descriptor).build();
      return new AckServerResult(response, result.getOnDeliverySuccess(), result.getOnDeliveryFail());
    } catch (InvalidProtocolBufferException e) {
      logger.warn("received malformed message", e);
      return new AckServerResult(ChannelFactoryReply.getDefaultInstance(), () -> {}, () -> {});
    }

  }
}
