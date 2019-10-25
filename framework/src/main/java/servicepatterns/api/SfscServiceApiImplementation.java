package servicepatterns.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Publisher;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Subscriber;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleClient;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleServer;
import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.framework.descriptor.PublisherTags;
import de.unistuttgart.isw.sfsc.framework.descriptor.RegexDefinition;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servicepatterns.api.tagging.Tagger;
import servicepatterns.basepatterns.ackreqrep.AckClient;
import servicepatterns.basepatterns.ackreqrep.AckServer;
import servicepatterns.basepatterns.ackreqrep.AckServerResult;
import servicepatterns.services.channelfactory.ChannelFactoryServer;

final class SfscServiceApiImplementation implements SfscServiceApi {

  private static final Logger logger = LoggerFactory.getLogger(SfscServiceApiImplementation.class);
  private final ExecutorService executorService = Executors.unconfigurableExecutorService(
      Executors.newCachedThreadPool(new ExceptionLoggingThreadFactory(getClass().getName(), logger))); //todo all to unconfigurable?
  private final ScheduledExecutorService ackServerScheduledExecutorService = Executors.unconfigurableScheduledExecutorService(
      Executors.newScheduledThreadPool(0, new ExceptionLoggingThreadFactory(getClass().getName(), logger)));

  private final PubSubConnection pubSubConnection;
  private final ApiRegistryManager apiRegistryManager;
  private final String coreId;
  private final String adapterId;

  SfscServiceApiImplementation(Adapter adapter) {
    apiRegistryManager = new ApiRegistryManager(adapter.registryClient());
    pubSubConnection = adapter.dataConnection();
    coreId = adapter.adapterInformation().getCoreId();
    adapterId = adapter.adapterInformation().getAdapterId();
  }

  @Override
  public Set<Map<String, ByteString>> getServices() {
    return apiRegistryManager.getServices();
  }

  @Override
  public Set<Map<String, ByteString>> getServices(String name) {
    return apiRegistryManager.getServices(name);
  }

  @Override
  public Set<Map<String, ByteString>> getServices(String name, Message message, Collection<String> varPaths) {
    return apiRegistryManager.getServices(name, message, varPaths);
  }

  @Override
  public SfscServer server(String name, ByteString inputMessageType, ByteString serverTopic, ByteString outputMessageType,
      RegexDefinition regexDefinition,
      Map<String, ByteString> customTags, Function<ByteString, AckServerResult> serverFunction, int timeoutMs, int sendRateMs, int sendMaxTries) {

    AckServer server = new AckServer(pubSubConnection, ackServerScheduledExecutorService, serverFunction, serverTopic, timeoutMs, sendRateMs,
        sendMaxTries,
        executorService);
    Map<String, ByteString> tags = Tagger
        .createServerTags(name, UUID.randomUUID().toString(), adapterId, coreId, serverTopic, inputMessageType, outputMessageType, regexDefinition,
            customTags);
    Handle handle = apiRegistryManager.registerService(tags);
    return new SfscServerImplementation(tags, () -> {
      handle.close();
      server.close();
    });
  }

  @Override
  public SfscClient client() {
    ByteString simpleTopic = ByteString.copyFromUtf8(UUID.randomUUID().toString());
    ByteString ackTopic = ByteString.copyFromUtf8(UUID.randomUUID().toString());
    return new SfscClientImplementation(this,
        new SimpleClient(pubSubConnection, simpleTopic, executorService),
        new AckClient(pubSubConnection, ackTopic, executorService));
  }

  @Override
  public SfscPublisher publisher(String name, ByteString outputTopic, ByteString outputMessageType, Map<String, ByteString> customTags) {
    Map<String, ByteString> tags = Tagger
        .createPublisherTags(name, UUID.randomUUID().toString(), adapterId, coreId, outputTopic, outputMessageType, customTags);
    Handle handle = apiRegistryManager.registerService(tags);
    Publisher publisher = new Publisher(pubSubConnection);
    return new SfscPublisherImplementation(tags, outputTopic, publisher, executorService, handle::close);
  }

  @Override
  public SfscPublisher unregisteredPublisher(String name, ByteString outputTopic, ByteString outputMessageType, Map<String, ByteString> customTags) {
    Map<String, ByteString> tags = Tagger
        .createPublisherTags(name, UUID.randomUUID().toString(), adapterId, coreId, outputTopic, outputMessageType, customTags);
    Publisher publisher = new Publisher(pubSubConnection);
    return new SfscPublisherImplementation(tags, outputTopic, publisher, executorService, () -> {});
  }

  @Override
  public SfscSubscriber subscriber(Map<String, ByteString> publisherTags, Consumer<ByteString> subscriberConsumer) {
    ByteString publisherTopic = publisherTags.get(PublisherTags.SFSC_PUBLISHER_OUTPUT_TOPIC.name());
    return subscriber(publisherTopic, subscriberConsumer);
  }

  @Override
  public SfscSubscriber subscriber(ByteString publisherTopic, Consumer<ByteString> subscriberConsumer) {
    Subscriber subscriber = new Subscriber(pubSubConnection, subscriberConsumer, publisherTopic, executorService);
    return new SfscSubscriberImplementation(subscriber::close);
  }

  @Override
  public SfscServer channelGenerator(String name, Map<String, ByteString> customTags, ByteString serverTopic, ByteString inputMessageType,
      Function<ByteString, SfscPublisher> channelFactory) {
    ChannelFactoryServer channelFactoryServer = new ChannelFactoryServer(channelFactory);
    SimpleServer simpleServer = new SimpleServer(pubSubConnection, channelFactoryServer, serverTopic, executorService);
    Map<String, ByteString> tags = Tagger
        .createChannelFactoryTags(name, UUID.randomUUID().toString(), adapterId, coreId, serverTopic, inputMessageType, customTags);
    Handle handle = apiRegistryManager.registerService(tags);

    return new SfscServerImplementation(tags, () -> {
      handle.close();
      simpleServer.close();
    });
  }

  public Handle addServiceAddedListener(Consumer<Map<String, ByteString>> listener) {
    return apiRegistryManager.addServiceAddedListener(listener);
  }

  public Handle addServiceRemovedListener(Consumer<Map<String, ByteString>> listener) {
    return apiRegistryManager.addServiceAddedListener(listener);
  }

  //todo close
}
