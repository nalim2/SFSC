package servicepatterns.reqrep;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import de.unistuttgart.isw.sfsc.patterns.SfscError;
import de.unistuttgart.isw.sfsc.patterns.reqrep.RequestReplyMessage;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servicepatterns.SfscMessage;
import servicepatterns.SfscMessageImpl;

class ServerConsumer implements Consumer<SfscMessage> {

  private static final Logger logger = LoggerFactory.getLogger(ServerConsumer.class);

  private final Function<SfscMessage, ByteString> server;
  private final OutputPublisher publisher;

  ServerConsumer(Function<SfscMessage, ByteString> server, OutputPublisher publisher) {
    this.server = server;
    this.publisher = publisher;
  }

  @Override
  public void accept(SfscMessage sfscMessage) {
    if (sfscMessage.hasError()) {
      logger.warn("Received message with error {}", sfscMessage.getError());
    } else {
      try {
        RequestReplyMessage request = RequestReplyMessage.parseFrom(sfscMessage.getPayload());
        SfscMessage processedSfscMessage = new SfscMessageImpl(SfscError.NO_ERROR, request.getPayload());
        ByteString payload = server.apply(processedSfscMessage);
        publisher.publish(request.getResponseTopic(),
            RequestReplyMessage.newBuilder()
                .setMessageId(request.getMessageId())
                .setPayload(payload)
                .clearResponseTopic()
                .build()
                .toByteArray()
        );
      } catch (InvalidProtocolBufferException e) {
        logger.warn("Received malformed message", e);
      }
    }
  }
}
