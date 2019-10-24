package de.unistuttgart.isw.sfsc.core.control;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnectionImplementation;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubSocketPair;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ContextConfiguration;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import de.unistuttgart.isw.sfsc.core.control.bootstrapping.BootstrapModule;
import de.unistuttgart.isw.sfsc.core.control.registry.RegistryModule;
import de.unistuttgart.isw.sfsc.core.control.session.SessionModule;
import de.unistuttgart.isw.sfsc.core.hazelcast.registry.Registry;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Control implements NotThrowingAutoCloseable {

  private final Reactor reactor;
  private final PubSubSocketPair pubSubSocketPair;
  private final PubSubConnectionImplementation pubSubConnection;
  private final RegistryModule registryModule;
  private final SessionModule sessionModule;
  private final BootstrapModule bootstrapModule;

  Control(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration, Registry registry)
      throws ExecutionException, InterruptedException {
    String coreId = UUID.randomUUID().toString();

    reactor = Reactor.create(contextConfiguration);
    pubSubSocketPair = PubSubSocketPair.create(reactor);
    pubSubSocketPair.publisherSettings().setXPubVerbose();

    pubSubConnection = PubSubConnectionImplementation.create(pubSubSocketPair);

    registryModule = RegistryModule.create(pubSubConnection, registry);
    sessionModule = SessionModule.create(pubSubConnection, configuration, coreId);
    bootstrapModule = BootstrapModule.create(pubSubConnection, configuration);

    pubSubConnection.start();
  }

  public static Control create(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration,Registry registry)
      throws ExecutionException, InterruptedException {
    Control control = new Control(contextConfiguration, configuration, registry);
    control.pubSubSocketPair.publisherSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.CONTROL_PUB_PORT)));
    control.pubSubSocketPair.subscriberSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.CONTROL_SUB_PORT)));
    return control;
  }

  @Override
  public void close() {
    reactor.close();
    pubSubConnection.close();
    pubSubSocketPair.close();
    bootstrapModule.close();
    sessionModule.close();
    registryModule.close();
  }
}
