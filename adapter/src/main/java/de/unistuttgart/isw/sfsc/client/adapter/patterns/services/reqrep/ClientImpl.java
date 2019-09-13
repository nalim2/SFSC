package de.unistuttgart.isw.sfsc.client.adapter.patterns.services.reqrep;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.registry.TimeoutRegistry;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.patterns.SfscError;
import de.unistuttgart.isw.sfsc.patterns.reqrep.RequestReplyMessage;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor.Tags;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.SfscMessage;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.SfscMessageImpl;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.BaseService;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

class ClientImpl extends BaseService implements Client {

  private final PubSubConnection.Publisher publisher;
  private final TimeoutRegistry<Integer, Consumer<SfscMessage>> timeoutRegistry;
  private final byte[] serverTopicCache;
  private final Supplier<Integer> idGenerator;
  private final ByteString clientTopic;

  ClientImpl(Map<String, ByteString> tags, Supplier<Integer> idGenerator, TimeoutRegistry<Integer, Consumer<SfscMessage>> timeoutRegistry, ByteString clientTopic,
      PubSubConnection.Publisher publisher, Runnable closer) {
    super(tags, closer);
    this.publisher = publisher;
    this.idGenerator = idGenerator;
    this.timeoutRegistry = timeoutRegistry;
    this.serverTopicCache = getTags().get(Tags.TOPIC.name()).toByteArray();
    this.clientTopic = clientTopic;
  }

  @Override
  public void send(byte[] payload, Consumer<SfscMessage> consumer, int timoutMs) {
    int id = idGenerator.get();
    RequestReplyMessage requestReplyMessage = RequestReplyMessage.newBuilder().setMessageId(id).setPayload(ByteString.copyFrom(payload))
        .setResponseTopic(clientTopic).build();
    timeoutRegistry.put(id, consumer, timoutMs, () -> consumer.accept(new SfscMessageImpl(SfscError.TIMEOUT, null)));
    publisher.publish(serverTopicCache, requestReplyMessage);
  }
}
