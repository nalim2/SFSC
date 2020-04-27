package de.unistuttgart.isw.sfsc.core.control;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnectionImplementation;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubSocketPair;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Connector;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactorFactory;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.TransportProtocol;
import de.unistuttgart.isw.sfsc.core.CoreParameter;
import de.unistuttgart.isw.sfsc.core.control.bootstrapping.BootstrapModule;
import de.unistuttgart.isw.sfsc.core.control.bootstrapping.BootstrapperParameter;
import de.unistuttgart.isw.sfsc.core.control.registry.RegistryModule;
import de.unistuttgart.isw.sfsc.core.control.registry.RegistryParameter;
import de.unistuttgart.isw.sfsc.core.control.session.SessionModule;
import de.unistuttgart.isw.sfsc.core.control.session.SessionParameter;
import de.unistuttgart.isw.sfsc.core.hazelcast.registry.Registry;
import java.util.concurrent.ExecutionException;

public class Control implements NotThrowingAutoCloseable {

  private final Reactor reactor;
  private final PubSubSocketPair pubSubSocketPair;
  private final PubSubConnectionImplementation pubSubConnection;
  private final RegistryModule registryModule;
  private final SessionModule sessionModule;
  private final BootstrapModule bootstrapModule;

  Control(CoreParameter parameter, Registry registry) throws ExecutionException, InterruptedException {

    BootstrapperParameter bootstrapperParameter = new BootstrapperParameter(parameter.getBootstrapTopic(), parameter.getControlSubPort());
    RegistryParameter registryParameter = new RegistryParameter(parameter.getRegistryQueryTopic(), parameter.getRegistryCommandTopic(),
        parameter.getRegistryPublisherTopic());
    SessionParameter sessionParameter = new SessionParameter(parameter.getCoreId(), parameter.getHeartbeatSendRateMs(), parameter.getHeartbeatTopic(),
        parameter.getHeartbeatDeadlineIncomingMs(), parameter.getSessionTopic(), parameter.getDataPubPort(), parameter.getDataSubPort());

    reactor = ReactorFactory.create();
    pubSubSocketPair = PubSubSocketPair.create(reactor);
    pubSubSocketPair.publisherSettings().setXPubVerbose();

    pubSubConnection = PubSubConnectionImplementation.create(pubSubSocketPair);

    registryModule = RegistryModule.create(pubSubConnection, registry, registryParameter);
    sessionModule = SessionModule.create(pubSubConnection, sessionParameter, registryModule::deleteAdapterEntries);
    bootstrapModule = BootstrapModule.create(pubSubConnection, bootstrapperParameter);

    pubSubConnection.start();
  }

  public static Control create(CoreParameter coreParameter, Registry registry)
      throws ExecutionException, InterruptedException {
    Control control = new Control(coreParameter, registry);
    control.pubSubSocketPair.publisherSocketConnector()
        .bind(TransportProtocol.TCP, Connector.createWildcardAddress(coreParameter.getControlPubPort()));
    control.pubSubSocketPair.subscriberSocketConnector()
        .bind(TransportProtocol.TCP, Connector.createWildcardAddress(coreParameter.getControlSubPort()));
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
