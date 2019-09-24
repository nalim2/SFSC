package servicepatterns.reqrep;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import servicepatterns.Service;
import servicepatterns.SfscMessage;
import servicepatterns.pubsub.PubSubFactory;

public class ReqRepFactory {

  private final OutputPublisher publisher;
  private final PubSubFactory pubSubFactory;

  public ReqRepFactory(Adapter adapter) {
    this.pubSubFactory = new PubSubFactory(adapter);
    this.publisher = adapter.publisher();
  }

  public Service server(ByteString inputTopic, Map<String, ByteString> customTags, Function<SfscMessage, byte[]> serverFunction, Executor executor) {
    Consumer<SfscMessage> consumer = new ServerConsumer(serverFunction, publisher);
    return pubSubFactory.subscriber(inputTopic, customTags, consumer, executor);
  }

  public Client client(ByteString responseTopic, Map<String, ByteString> customTags, Executor executor) {
    ClientConsumer clientConsumer = new ClientConsumer();
    Service subscriber = pubSubFactory.subscriber(responseTopic, customTags, clientConsumer, executor);
    return new ClientService(publisher, subscriber.getTags(), clientConsumer, () -> {
      subscriber.close();
      clientConsumer.close();
    });
  }
}
