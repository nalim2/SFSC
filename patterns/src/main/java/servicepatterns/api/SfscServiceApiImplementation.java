package servicepatterns.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import de.unistuttgart.isw.sfsc.commonjava.zmq.subscriptiontracker.SubscriptionTracker;
import de.unistuttgart.isw.sfsc.patterns.tags.PublisherTags;
import de.unistuttgart.isw.sfsc.patterns.tags.RegexDefinition;
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
import servicepatterns.api.registry.ApiRegistryManager;
import servicepatterns.api.registry.Registration;
import servicepatterns.api.tagging.Tagger;
import servicepatterns.basepatterns.ackreqrep.AckClient;
import servicepatterns.basepatterns.ackreqrep.AckServer;
import servicepatterns.basepatterns.ackreqrep.AckServerResult;
import servicepatterns.basepatterns.pubsub.Subscriber;
import servicepatterns.basepatterns.simplereqrep.SimpleClient;
import servicepatterns.basepatterns.simplereqrep.SimpleServer;
import servicepatterns.services.channelfactory.ChannelFactoryFunction;
import servicepatterns.services.channelfactory.ChannelFactoryResult;
import servicepatterns.topiclistener.HandleFactory;

final class SfscServiceApiImplementation implements SfscServiceApi {

  private static final Logger logger = LoggerFactory.getLogger(SfscServiceApiImplementation.class);
  private final ExecutorService executorService = Executors.unconfigurableExecutorService(
      Executors.newCachedThreadPool(new ExceptionLoggingThreadFactory(getClass().getName(), logger))); //todo all to unconfigurable?
  private final ScheduledExecutorService ackServerScheduledExecutorService = Executors.unconfigurableScheduledExecutorService(
      Executors.newScheduledThreadPool(0, new ExceptionLoggingThreadFactory(getClass().getName(), logger)));
  private final Tagger tagger = new Tagger();
  private final HandleFactory handleFactory;
  private final SubscriptionTracker subscriptionTracker;
  private final ApiRegistryManager apiRegistryManager;
  private final OutputPublisher publisher;
  private final UUID coreId;
  private final UUID adapterId;

  SfscServiceApiImplementation(Adapter adapter) {
    apiRegistryManager = new ApiRegistryManager(adapter.registryClient());
    handleFactory = new HandleFactory(adapter.inboxTopicManager());
    subscriptionTracker = adapter.subscriptionTracker();
    publisher = adapter.publisher();
    coreId = UUID.randomUUID();//UUID.fromString(adapter.coreId()); todo
    adapterId = UUID.randomUUID();//UUID.fromString(adapter.adapterId()); todo
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

    AckServer server = new AckServer(publisher, ackServerScheduledExecutorService, serverFunction, serverTopic, timeoutMs, sendRateMs, sendMaxTries,
        handleFactory, executorService);
    Map<String, ByteString> tags = tagger
        .createServerTags(name, UUID.randomUUID(), adapterId, coreId, serverTopic, inputMessageType, outputMessageType, regexDefinition, customTags);
    Registration registration = apiRegistryManager.registerService(tags);
    return new SfscServerImplementation(tags, () -> {
      registration.close();
      server.close();
    });
  }


  @Override
  public SfscClient client() {
    ByteString simpleTopic = ByteString.copyFromUtf8(UUID.randomUUID().toString());
    ByteString ackTopic = ByteString.copyFromUtf8(UUID.randomUUID().toString());
    return new SfscClientImplementation(this,
        new SimpleClient(publisher, simpleTopic, handleFactory, executorService),
        new AckClient(publisher, ackTopic, handleFactory, executorService));
  }

  @Override
  public SfscPublisher publisher(String name, ByteString outputTopic, ByteString outputMessageType, Map<String, ByteString> customTags) {
    Map<String, ByteString> tags = tagger
        .createPublisherTags(name, UUID.randomUUID(), adapterId, coreId, outputTopic, outputMessageType, customTags);
    Registration registration = apiRegistryManager.registerService(tags);
    return new SfscPublisherImplementation(tags, outputTopic, publisher, registration::close);
  }

  @Override
  public SfscPublisher unregisteredPublisher(String name, ByteString outputTopic, ByteString outputMessageType, Map<String, ByteString> customTags) {
    Map<String, ByteString> tags = tagger
        .createPublisherTags(name, UUID.randomUUID(), adapterId, coreId, outputTopic, outputMessageType, customTags);
    return new SfscPublisherImplementation(tags, outputTopic, publisher, () -> {});
  }

  @Override
  public SfscSubscriber subscriber(Map<String, ByteString> publisherTags, Consumer<ByteString> subscriberConsumer) {
    ByteString publisherTopic = publisherTags.get(PublisherTags.SFSC_PUBLISHER_OUTPUT_TOPIC.name());
    return subscriber(publisherTopic, subscriberConsumer);
  }

  @Override
  public SfscSubscriber subscriber(ByteString publisherTopic, Consumer<ByteString> subscriberConsumer) {
    Subscriber subscriber = new Subscriber(subscriberConsumer, publisherTopic, executorService, handleFactory);
    return new SfscSubscriberImplementation(subscriber::close);
  }

  @Override
  public SfscServer channelGenerator(String name, Map<String, ByteString> customTags, ByteString serverTopic, ByteString inputMessageType,
      Function<ByteString, ChannelFactoryResult> channelFactory) {
    ChannelFactoryFunction channelFactoryFunction = new ChannelFactoryFunction(channelFactory, subscriptionTracker, executorService);
    SimpleServer simpleServer = new SimpleServer(publisher, handleFactory, channelFactoryFunction, serverTopic, executorService);
    Map<String, ByteString> tags = tagger
        .createChannelFactoryTags(name, UUID.randomUUID(), adapterId, coreId, serverTopic, inputMessageType, customTags);
    Registration registration = apiRegistryManager.registerService(tags);

    return new SfscServerImplementation(tags, () -> {
      registration.close();
      simpleServer.close();
    });
  }
}
