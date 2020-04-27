package de.unistuttgart.isw.sfsc.adapter.control.registry;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.adapter.control.RegistryApi;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.command.CommandReply;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Subscriber;
import de.unistuttgart.isw.sfsc.commonjava.util.FutureAdapter;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryModule implements RegistryApi {

  private static final Logger logger = LoggerFactory.getLogger(RegistryModule.class);

  private final Registry registry;

  private final CommandClient commandClient;
  private final QueryClient queryClient;
  private final Subscriber subscriber;
  private final RegistryParameter params;

  RegistryModule(RegistryParameter parameter, PubSubConnection pubSubConnection, ScheduledExecutorService executor) {
    this.params = parameter;
    registry = new Registry(executor);
    queryClient = new QueryClient(pubSubConnection, parameter.getCoreQueryTopic(), parameter.getAdapterQueryTopic(), parameter.getTimeoutMs(),
        executor);
    commandClient = new CommandClient(pubSubConnection, parameter.getCoreCommandTopic(), parameter.getAdapterCommandTopic(),
        parameter.getTimeoutMs(), executor);
    registry.addNotificationListener(
        () -> queryClient.query(registry::getId, registry::handleQueryReply, () -> logger.warn("registry query timeout"))
    );
    subscriber = new Subscriber(pubSubConnection, registry::handleQueryReply, parameter.getCoreEventTopic(), executor);
    executor.scheduleAtFixedRate(
        () -> queryClient.query(registry::getId, registry::handleQueryReply, () -> logger.warn("registry query timeout")),
        0, parameter.getPollingRateMs(), TimeUnit.MILLISECONDS);
  }

  public static RegistryModule create(RegistryParameter parameter, PubSubConnection pubSubConnection, ScheduledExecutorService executor) {
    return new RegistryModule(parameter, pubSubConnection, executor);
  }

  @Override
  public Future<CommandReply> create(ByteString entry) {
    FutureAdapter<ByteString, CommandReply> future = new FutureAdapter<>(CommandReply::parseFrom, () -> {throw new TimeoutException();});
    commandClient.create(entry, params.getAdapterId(), future::handleInput, future::handleError);
    return future;
  }

  @Override
  public Future<CommandReply> remove(ByteString entry) {
    FutureAdapter<ByteString, CommandReply> future = new FutureAdapter<>(CommandReply::parseFrom, () -> {throw new TimeoutException();});
    commandClient.remove(entry, params.getAdapterId(), future::handleInput, future::handleError);
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
  }
}
