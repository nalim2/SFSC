package de.unistuttgart.isw.sfsc.framework.api.services.channelfactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.framework.api.services.pubsub.SfscPublisher;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.protocol.channelfactory.ChannelFactoryReply;
import de.unistuttgart.isw.sfsc.framework.protocol.channelfactory.ChannelFactoryRequest;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelFactoryServer implements Function<ByteString, ByteString> {

  private static final Logger logger = LoggerFactory.getLogger(ChannelFactoryServer.class);

  private final Function<ByteString, SfscPublisher> channelFactory;

  public ChannelFactoryServer(Function<ByteString, SfscPublisher> channelFactory) {
    this.channelFactory = channelFactory;
  }

  @Override
  public ByteString apply(ByteString byteString) {
    try {
      ChannelFactoryRequest request = ChannelFactoryRequest.parseFrom(byteString);
      ByteString payload = request.getPayload();
      SfscPublisher sfscPublisher = channelFactory.apply(payload);
      if (sfscPublisher != null) {
        SfscServiceDescriptor descriptor = sfscPublisher.getDescriptor();
        return ChannelFactoryReply.newBuilder().setServiceDescriptor(descriptor).build().toByteString();
      } else {
        return ChannelFactoryReply.getDefaultInstance().toByteString();
      }
    } catch (InvalidProtocolBufferException e) {
      logger.warn("received malformed message", e);
      return ChannelFactoryReply.getDefaultInstance().toByteString();
    }
  }
}
