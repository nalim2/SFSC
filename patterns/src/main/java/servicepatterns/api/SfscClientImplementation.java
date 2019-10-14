package servicepatterns.api;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.ConsumerFuture;
import de.unistuttgart.isw.sfsc.patterns.tags.ChannelFactoryTags;
import de.unistuttgart.isw.sfsc.patterns.tags.ServerTags;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import servicepatterns.basepatterns.ackreqrep.AckClient;
import servicepatterns.basepatterns.simplereqrep.SimpleClient;
import servicepatterns.services.channelfactory.ChannelFactoryClient;

final class SfscClientImplementation implements SfscClient {

  private final SfscServiceApi sfscServiceApi;
  private final AckClient ackClient;
  private final SimpleClient simpleClient;

  SfscClientImplementation(SfscServiceApi sfscServiceApi, SimpleClient simpleClient, AckClient ackClient) {
    this.sfscServiceApi = sfscServiceApi;
    this.simpleClient = simpleClient;
    this.ackClient = ackClient;
  }

  @Override
  public void request(Map<String, ByteString> serverTags, ByteString payload, Consumer<ByteString> consumer, int timeoutMs,
      Runnable timeoutRunnable) {
    ByteString serverTopic = serverTags.get(ServerTags.SFSC_SERVER_INPUT_TOPIC.name());
    request(serverTopic, payload, consumer, timeoutMs, timeoutRunnable);
  }

  @Override
  public void request(ByteString serverTopic, ByteString payload, Consumer<ByteString> consumer, int timeoutMs, Runnable timeoutRunnable) {
    ackClient.send(serverTopic, payload, consumer, timeoutMs, timeoutRunnable);
  }

  @Override
  public Future<SfscSubscriber> requestChannel(Map<String, ByteString> channelFactoryTags, ByteString payload, int timeoutMs,
      Runnable timeoutRunnable, Consumer<ByteString> consumer) {
    ByteString channelFactoryTopic = channelFactoryTags.get(ChannelFactoryTags.SFSC_CHANNEL_FACTORY_INPUT_TOPIC.name());
    return requestChannel(channelFactoryTopic, payload, timeoutMs, timeoutRunnable, consumer);
  }

  @Override
  public Future<SfscSubscriber> requestChannel(ByteString channelFactoryTopic, ByteString payload, int timeoutMs, Runnable timeoutRunnable,
      Consumer<ByteString> consumer) {
    ConsumerFuture<ByteString, SfscSubscriber> consumerFuture = new ConsumerFuture<>(new ChannelFactoryClient(sfscServiceApi, consumer));
    simpleClient.send(channelFactoryTopic, ChannelFactoryClient.getMessage(payload), consumerFuture, timeoutMs, timeoutRunnable);
    return consumerFuture;
  }
}
