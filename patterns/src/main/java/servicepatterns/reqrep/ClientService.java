package servicepatterns.reqrep;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import de.unistuttgart.isw.sfsc.patterns.Tags;
import de.unistuttgart.isw.sfsc.patterns.reqrep.RequestReplyMessage;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import servicepatterns.ServiceImpl;
import servicepatterns.SfscMessage;

public class ClientService extends ServiceImpl implements Client {

  private final Supplier<Integer> idGenerator = new AtomicInteger()::getAndIncrement;
  private final ClientConsumer clientConsumer;
  private final OutputPublisher publisher;
  private final ByteString responseTopic;

  public ClientService(OutputPublisher publisher, Map<String, ByteString> tags, ClientConsumer clientConsumer, Runnable closer) {
    super(tags, closer);
    this.publisher = publisher;
    this.clientConsumer = clientConsumer;
    this.responseTopic = tags.get(Tags.INPUT_TOPIC.name());
  }

  @Override
  public void send(Map<String, ByteString> serverTags, ByteString payload, Consumer<SfscMessage> consumer, int timoutMs) {
    int id = idGenerator.get();
    RequestReplyMessage requestReplyMessage = RequestReplyMessage.newBuilder()
        .setMessageId(id)
        .setPayload(payload)
        .setResponseTopic(responseTopic)
        .build();
    clientConsumer.addCallback(id, consumer, timoutMs);
    publisher.publish(serverTags.get(Tags.INPUT_TOPIC.name()), requestReplyMessage.toByteArray());
  }

}
