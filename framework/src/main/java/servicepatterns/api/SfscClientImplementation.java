package servicepatterns.api;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleClient;
import de.unistuttgart.isw.sfsc.commonjava.util.FutureAdapter;
import de.unistuttgart.isw.sfsc.framework.descriptor.ChannelFactoryTags;
import de.unistuttgart.isw.sfsc.framework.descriptor.ServerTags;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import servicepatterns.basepatterns.ackreqrep.AckClient;
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
  public Future<SfscSubscriber> requestChannel(Map<String, ByteString> channelFactoryTags, ByteString payload, int timeoutMs, Consumer<ByteString> consumer) {
    ByteString channelFactoryTopic = channelFactoryTags.get(ChannelFactoryTags.SFSC_CHANNEL_FACTORY_INPUT_TOPIC.name());
    return requestChannel(channelFactoryTopic, payload, timeoutMs, consumer);
  }

  @Override
  public Future<SfscSubscriber> requestChannel(ByteString channelFactoryTopic, ByteString payload, int timeoutMs, Consumer<ByteString> consumer) {
    ChannelFactoryClient channelFactoryClient = new ChannelFactoryClient(sfscServiceApi, consumer);
    FutureAdapter<ByteString, SfscSubscriber> futureAdapter = new FutureAdapter<>(channelFactoryClient::process, channelFactoryClient::handleTimeout);
    simpleClient.send(channelFactoryTopic, channelFactoryClient.getMessage(payload), futureAdapter::handleInput, timeoutMs, futureAdapter::handleError);
    return futureAdapter;
  }
}
