package de.unistuttgart.isw.sfsc.framework.api.services.clientserver;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleClient;
import de.unistuttgart.isw.sfsc.commonjava.util.FutureAdapter;
import de.unistuttgart.isw.sfsc.framework.api.SfscServiceApi;
import de.unistuttgart.isw.sfsc.framework.api.services.ServiceFactory;
import de.unistuttgart.isw.sfsc.framework.api.services.channelfactory.ChannelFactoryClient;
import de.unistuttgart.isw.sfsc.framework.api.services.pubsub.SfscSubscriber;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep.AckClient;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public final class SfscClientImplementation implements SfscClient {

  private final SfscServiceApi sfscServiceApi;
  private final AckClient serverClient;
  private final SimpleClient requestChannelClient;

  public SfscClientImplementation(SfscServiceApi sfscServiceApi, ServiceFactory serviceFactory) {
    this.sfscServiceApi = sfscServiceApi;
    this.serverClient = new AckClient(serviceFactory.pubSubConnection(), serviceFactory.createTopic(), serviceFactory.executorService());
    this.requestChannelClient = new SimpleClient(serviceFactory.pubSubConnection(), serviceFactory.createTopic(), serviceFactory.executorService());
  }

  @Override
  public void request(SfscServiceDescriptor serverDescriptor, Message payload, Consumer<ByteString> consumer, int timeoutMs,
      Runnable timeoutRunnable) {
    ByteString serverTopic = serverDescriptor.getServerTags().getInputTopic();
    serverClient.send(serverTopic, payload, consumer, timeoutMs, timeoutRunnable);
  }

  @Override
  public Future<SfscSubscriber> requestChannel(SfscServiceDescriptor channelFactoryTags, ByteString payload, int timeoutMs, Consumer<ByteString> consumer) {
    ByteString channelFactoryTopic = channelFactoryTags.getChannelFactoryTags().getInputTopic();
    ChannelFactoryClient channelFactoryClient = new ChannelFactoryClient(sfscServiceApi, consumer);
    FutureAdapter<ByteString, SfscSubscriber> futureAdapter = new FutureAdapter<>(channelFactoryClient::process, channelFactoryClient::handleTimeout);
    requestChannelClient
        .send(channelFactoryTopic, channelFactoryClient.getMessage(payload), futureAdapter::handleInput, timeoutMs, futureAdapter::handleError);
    return futureAdapter;
  }
}
