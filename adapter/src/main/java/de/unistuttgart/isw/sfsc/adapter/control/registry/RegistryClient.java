package de.unistuttgart.isw.sfsc.adapter.control.registry;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.adapter.control.session.WelcomeInformation;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.command.CommandReply;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Subscriber;
import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.util.FutureAdapter;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryClient implements RegistryApi {

  private static final String QUERY_SERVER_TOPIC = "REGISTRY_QUERY_SERVER";
  private static final String COMMAND_SERVER_TOPIC = "REGISTRY_COMMAND_SERVER";
  private static final String EVENT_PUBLISHER_TOPIC = "REGISTRY_EVENT_PUBLISHER";
  private static final int POLLING_RATE_MS = 1000;
  private static final int TIMEOUT_MS = 1000;

  private static final Logger logger = LoggerFactory.getLogger(RegistryClient.class);

  private final ScheduledExecutorService registryExecutor = Executors
      .newSingleThreadScheduledExecutor(new ExceptionLoggingThreadFactory(getClass().getName(), logger));
  private final Registry registry = new Registry(registryExecutor);

  private final CommandClient commandClient;
  private final QueryClient queryClient;
  private final Subscriber subscriber;
  private final WelcomeInformation welcomeInformation;

  RegistryClient(PubSubConnection pubSubConnection, WelcomeInformation welcomeInformation) {

    this.welcomeInformation = welcomeInformation;

    ByteString queryServerTopic = ByteString.copyFromUtf8(QUERY_SERVER_TOPIC);
    ByteString commandServerTopic = ByteString.copyFromUtf8(COMMAND_SERVER_TOPIC);
    ByteString eventPublisherTopic = ByteString.copyFromUtf8(EVENT_PUBLISHER_TOPIC);
    ByteString queryClientTopic = ByteString.copyFromUtf8("REGISTRY_QUERY_CLIENT_" + welcomeInformation.getAdapterId());
    ByteString commandClientTopic = ByteString.copyFromUtf8("REGISTRY_SERVER_CLIENT_" + welcomeInformation.getAdapterId());

    queryClient = new QueryClient(pubSubConnection, queryServerTopic, queryClientTopic, TIMEOUT_MS, registryExecutor);
    commandClient = new CommandClient(pubSubConnection, commandServerTopic, commandClientTopic, TIMEOUT_MS, registryExecutor);
    registry
        .addNotificationListener(() -> queryClient.query(registry::getId, registry::handleQueryReply, () -> logger.warn("registry query timeout")));
    subscriber = new Subscriber(pubSubConnection, registry::handleQueryReply, eventPublisherTopic, registryExecutor);
    registryExecutor
        .scheduleAtFixedRate(() -> queryClient.query(registry::getId, registry::handleQueryReply, () -> logger.warn("registry query timeout")),
            0, POLLING_RATE_MS, TimeUnit.MILLISECONDS);
  }

  public static RegistryClient create(PubSubConnection pubSubConnection, WelcomeInformation welcomeInformation) {
    return new RegistryClient(pubSubConnection, welcomeInformation);
  }

  @Override
  public Future<CommandReply> create(ByteString entry) {
    FutureAdapter<ByteString, CommandReply> future = new FutureAdapter<>(CommandReply::parseFrom, () -> {throw new TimeoutException();});
    commandClient.create(entry, welcomeInformation.getAdapterId(), future::handleInput, future::handleError);
    return future;
  }

  @Override
  public Future<CommandReply> remove(ByteString entry) {
    FutureAdapter<ByteString, CommandReply> future = new FutureAdapter<>(CommandReply::parseFrom, () -> {throw new TimeoutException();});
    commandClient.remove(entry, welcomeInformation.getAdapterId(), future::handleInput, future::handleError);
    return future;
  }

  @Override
  public Set<ByteString> getEntries() {
    return registry.getRegistry();
  }

  @Override
  public Handle addListener(Consumer<StoreEvent<ByteString>> listener) {
    return registry.addEntryListener(listener);
  }

  public void close() {
    queryClient.close();
    commandClient.close();
    subscriber.close();
    registryExecutor.shutdownNow();
  }
}
