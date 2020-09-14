package de.unistuttgart.isw.sfsc.core.control.registry;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.SchedulerService;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.core.hazelcast.registry.Registry;

public class RegistryModule implements NotThrowingAutoCloseable {

  private static final int REGISTRY_THREAD_NUMBER = 1;

  private final SchedulerService schedulerService = new SchedulerService(REGISTRY_THREAD_NUMBER);

  private final Registry registry;
  private final QueryServer queryServer;
  private final CommandServer commandServer;
  private final EventPublisher eventPublisher;

  RegistryModule(PubSubConnection pubSubConnection, Registry registry, RegistryParameter parameter) {
    eventPublisher = new EventPublisher(pubSubConnection, parameter.getEventPublisherTopic());
    registry.addEventListener(eventPublisher::send);
    commandServer = new CommandServer(pubSubConnection, parameter.getCommandServerTopic(), schedulerService, registry);
    queryServer = new QueryServer(pubSubConnection, parameter.getQueryServerTopic(), schedulerService, registry);
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
    schedulerService.close();
  }
}
