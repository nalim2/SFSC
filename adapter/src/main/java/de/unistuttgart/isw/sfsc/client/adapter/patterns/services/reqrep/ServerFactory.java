package de.unistuttgart.isw.sfsc.client.adapter.patterns.services.reqrep;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.Publisher;
import de.unistuttgart.isw.sfsc.patterns.SfscError;
import de.unistuttgart.isw.sfsc.patterns.reqrep.RequestReplyMessage;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.SfscMessage;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.SfscMessageImpl;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub.Subscriber;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub.SubscriberFactory;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.tags.TagCompleter;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerFactory {

  private final TagCompleter tagCompleter;
  private final SubscriberFactory subscriberFactory;
  private final PubSubConnection.Publisher publisher;

  public ServerFactory(TagCompleter tagCompleter, PubSubConnection.Publisher publisher, SubscriberFactory subscriberFactory) {
    this.tagCompleter = tagCompleter;
    this.publisher = publisher;
    this.subscriberFactory = subscriberFactory;
  }

  public Server server(Map<String, ByteString> tags, Function<SfscMessage, byte[]> server, Executor executor) {
    Map<String, ByteString> serverTags = tagCompleter.completeServer(tags);
    Subscriber subscriber = subscriberFactory.subscriber(serverTags, new ServerConsumer(server, publisher), executor);
    return new ServerImpl(serverTags, subscriber::close);
  }


  static class ServerConsumer implements Consumer<SfscMessage> {

    private static final Logger logger = LoggerFactory.getLogger(ServerConsumer.class);
    private final Function<SfscMessage, byte[]> server;
    private final PubSubConnection.Publisher publisher;

    ServerConsumer(Function<SfscMessage, byte[]> server, Publisher publisher) {
      this.server = server;
      this.publisher = publisher;
    }

    @Override
    public void accept(SfscMessage sfscMessage) {
      if (sfscMessage.getError() != null) {
        logger.warn("Received message with error {}", sfscMessage.getError());
      } else {
        try {
          RequestReplyMessage request = RequestReplyMessage.parseFrom(sfscMessage.getPayload());
          SfscMessage processedSfscMessage = new SfscMessageImpl(SfscError.NO_ERROR, request.getPayload());
          byte[] payload = server.apply(processedSfscMessage);
          publisher.publish(request.getResponseTopic().toByteArray(),
              RequestReplyMessage.newBuilder()
                  .setMessageId(request.getMessageId())
                  .setPayload(ByteString.copyFrom(payload))
                  .clearResponseTopic()
                  .build()
          );
        } catch (InvalidProtocolBufferException e) {
          logger.warn("Received malformed message", e);
        }
      }

    }
  }

}
