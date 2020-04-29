package de.unistuttgart.isw.sfsc.core.control.registry;

import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.core.hazelcast.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryModule implements NotThrowingAutoCloseable {

  private static final int REGISTRY_THREAD_NUMBER = 1;

  private static final Logger logger = LoggerFactory.getLogger(RegistryModule.class);

  private final ExecutorService executorService = Executors.unconfigurableExecutorService(
      Executors.newFixedThreadPool(REGISTRY_THREAD_NUMBER, new ExceptionLoggingThreadFactory(getClass().getName(), logger)));

  private final Registry registry;
  private final QueryServer queryServer;
  private final CommandServer commandServer;
  private final EventPublisher eventPublisher;

  RegistryModule(PubSubConnection pubSubConnection, Registry registry, RegistryParameter parameter) {
    eventPublisher = new EventPublisher(pubSubConnection, parameter.getEventPublisherTopic());
    registry.addEventListener(eventPublisher::send);
    commandServer = new CommandServer(pubSubConnection, parameter.getCommandServerTopic(), executorService, registry);
    queryServer = new QueryServer(pubSubConnection, parameter.getQueryServerTopic(), executorService, registry);
    this.registry = registry;
  }

  public static RegistryModule create(PubSubConnection pubSubConnection, Registry registry, RegistryParameter parameter) {
    return new RegistryModule(pubSubConnection, registry, parameter);
  }

  public void deleteAdapterEntries(String adapterId) {
    registry.deleteEntries(adapterId);
  }

  @Override
  public void close() {
    queryServer.close();
    commandServer.close();
    executorService.shutdownNow();
  }
}
