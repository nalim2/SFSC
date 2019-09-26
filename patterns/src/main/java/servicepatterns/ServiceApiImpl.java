package servicepatterns;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.adapter.base.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.commonjava.util.ConsumerFuture;
import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.patterns.Advanced_Tags;
import de.unistuttgart.isw.sfsc.patterns.RegexDefinition;
import de.unistuttgart.isw.sfsc.patterns.RegexDefinition.SfscType;
import de.unistuttgart.isw.sfsc.patterns.Tags;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servicepatterns.channelfactory.ChannelConsumer;
import servicepatterns.channelfactory.ChannelFactoryFunction;
import servicepatterns.pubsub.PubSubFactory;
import servicepatterns.pubsub.Publisher;
import servicepatterns.reqrep.Client;
import servicepatterns.reqrep.ReqRepFactory;

public class ServiceApiImpl implements ServiceApi {

  private static final Logger logger = LoggerFactory.getLogger(ServiceApiImpl.class);
  private final RegistryClient registryClient;
  private final PubSubFactory pubSubFactory;
  private final ReqRepFactory reqRepFactory;

  private final ExecutorService executorService = Executors.newCachedThreadPool(new ExceptionLoggingThreadFactory("HighLevelApi", logger));
  private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
  private Set<Map<String, ByteString>> services;

  public ServiceApiImpl(Adapter adapter) {
    registryClient = adapter.registryClient();
    pubSubFactory = new PubSubFactory(adapter);
    reqRepFactory = new ReqRepFactory(adapter);

    scheduledExecutorService.scheduleAtFixedRate(() -> {
      try {
        services = Collections.unmodifiableSet(registryClient.getServices().get());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }, 100, 100, TimeUnit.MILLISECONDS);
  }

  @Override
  public Set<Map<String, ByteString>> getServices() {
    return services;
  }

  @Override
  public Set<Map<String, ByteString>> getServices(String name) {
    return services.stream()
        .filter(FilterFactory.stringEqualsFilter(Advanced_Tags.NAME.name(), name))
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public Set<Map<String, ByteString>> getServices(String name, Message message, Collection<String> varPaths) {
    return new RegexFilter().findMatching(getServices(name), message, varPaths);
  }

  @Override
  public Service server(String name, String inputMessageType, String outputMessageType, RegexDefinition regexDefinition,
      Map<String, ByteString> customTags, Function<SfscMessage, ByteString> serverFunction) {
    Map<String, ByteString> tags = new HashMap<>(customTags);
    tags.put(Advanced_Tags.NAME.name(), ByteString.copyFromUtf8(name));
    tags.put(Advanced_Tags.REGEX.name(), regexDefinition.toByteString());
    tags.put(Advanced_Tags.INPUT_MESSAGE_TYPE.name(), ByteString.copyFromUtf8(inputMessageType));
    tags.put(Advanced_Tags.OUTPUT_MESSAGE_TYPE.name(), ByteString.copyFromUtf8(outputMessageType));
    tags.put(Advanced_Tags.SFSC_TYPE.name(), ByteString.copyFromUtf8(SfscType.SERVER.name()));

    ByteString inputTopic = ByteString.copyFromUtf8(UUID.randomUUID().toString());

    Service server = reqRepFactory.server(inputTopic, tags, serverFunction, executorService);
    registryClient.addService(server.getTags());
    return server;
  }

  @Override
  public Client client() {
    ByteString responseTopic = ByteString.copyFromUtf8(UUID.randomUUID().toString());
    return reqRepFactory.client(responseTopic, Collections.emptyMap(), executorService);
  }

  @Override
  public Publisher publisher(String name, String outputMessageType, Map<String, ByteString> customTags) {
    Publisher publisher = unregisteredPublisher(name, outputMessageType, customTags);
    registryClient.addService(publisher.getTags());
    return publisher;
  }

  @Override
  public Publisher unregisteredPublisher(String name, String outputMessageType, Map<String, ByteString> customTags) {
    Map<String, ByteString> tags = new HashMap<>(customTags);
    tags.put(Advanced_Tags.NAME.name(), ByteString.copyFromUtf8(name));
    tags.put(Advanced_Tags.OUTPUT_MESSAGE_TYPE.name(), ByteString.copyFromUtf8(outputMessageType));
    tags.put(Advanced_Tags.SFSC_TYPE.name(), ByteString.copyFromUtf8(SfscType.PUBLISHER.name()));

    ByteString topicByteString = ByteString.copyFromUtf8(name);
    return pubSubFactory.publisher(topicByteString, tags);
  }

  @Override
  public Service subscriber(Map<String, ByteString> publisherTags, Consumer<SfscMessage> consumer) {
    ByteString publisherTopic = publisherTags.get(Tags.OUTPUT_TOPIC.name());
    return pubSubFactory.subscriber(publisherTopic, Collections.emptyMap(), consumer, executorService);
  }

  @Override
  public Service channelGenerator(String name, Map<String, ByteString> customTags, Function<SfscMessage, Publisher> channelFactory) {
    ChannelFactoryFunction channelFactoryFunction = new ChannelFactoryFunction(channelFactory);
    return server(name,
        "de.unistuttgart.isw.sfsc.patterns.channelfactory.ChannelFactoryRequest",
        "de.unistuttgart.isw.sfsc.patterns.channelfactory.ChannelFactoryReply",
        RegexDefinition.getDefaultInstance(),
        customTags,
        channelFactoryFunction);
  }

  @Override
  public Future<Service> requestChannel(Client client, Map<String, ByteString> channelGeneratorTags, ByteString payload, int timeoutMs, Consumer<SfscMessage> consumer) {
    ChannelConsumer channelConsumer = new ChannelConsumer(this, consumer);
    ConsumerFuture<SfscMessage, Service> consumerFuture = new ConsumerFuture<>(channelConsumer);
    client.send(channelGeneratorTags, channelConsumer.getMessage(payload), consumerFuture, timeoutMs);
    return consumerFuture;
  }
}
