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
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Control implements NotThrowingAutoCloseable {

  private final Reactor reactor;
  private final PubSubSocketPair pubSubSocketPair;
  private final PubSubConnectionImplementation pubSubConnection;
  private final RegistryModule registryModule;
  private final SessionModule sessionModule;
  private final BootstrapModule bootstrapModule;

  Control(CoreParameter parameter, Registry registry) throws ExecutionException, InterruptedException {

    BootstrapperParameter bootstrapperParameter = new BootstrapperParameter(parameter.getBootstrapTopic(),
        parameter.getControlPubTcpPort(),
        parameter.getControlSubTcpPort(),
        parameter.getDataPubTcpPort(),
        parameter.getDataSubTcpPort(),
        parameter.getControlPubIpcFile(),
        parameter.getControlSubIpcFile(),
        parameter.getDataPubIpcFile(),
        parameter.getDataSubIpcFile());
    RegistryParameter registryParameter = new RegistryParameter(parameter.getRegistryQueryTopic(), parameter.getRegistryCommandTopic(),
        parameter.getRegistryPublisherTopic());
    SessionParameter sessionParameter = new SessionParameter(parameter.getCoreId(), parameter.getHeartbeatSendRateMs(), parameter.getHeartbeatTopic(),
        parameter.getHeartbeatDeadlineIncomingMs(), parameter.getSessionTopic());

    reactor = ReactorFactory.create();
    pubSubSocketPair = PubSubSocketPair.create(reactor);
    pubSubSocketPair.publisherSettings().setXPubVerbose();

    pubSubConnection = PubSubConnectionImplementation.create(pubSubSocketPair);

    registryModule = RegistryModule.create(pubSubConnection, registry, registryParameter);
    sessionModule = SessionModule.create(pubSubConnection, sessionParameter, registryModule::deleteAdapterEntries);
    bootstrapModule = BootstrapModule.create(pubSubConnection, bootstrapperParameter);

    pubSubConnection.start();
  }

  public static Control create(CoreParameter parameter, Registry registry) throws ExecutionException, InterruptedException, IOException {
    Control control = new Control(parameter, registry);
    File pub = new File(parameter.getIpcFolderLocation(), parameter.getControlPubIpcFile());
    File sub = new File(parameter.getIpcFolderLocation(), parameter.getControlSubIpcFile());
    pub.createNewFile();
    sub.createNewFile();
    control.pubSubSocketPair.publisherSocketConnector().bind(TransportProtocol.IPC, pub.getAbsolutePath());
    control.pubSubSocketPair.subscriberSocketConnector().bind(TransportProtocol.IPC, sub.getAbsolutePath());
    control.pubSubSocketPair.publisherSocketConnector()
        .bind(TransportProtocol.TCP, Connector.createWildcardAddress(parameter.getControlPubTcpPort()));
    control.pubSubSocketPair.subscriberSocketConnector()
        .bind(TransportProtocol.TCP, Connector.createWildcardAddress(parameter.getControlSubTcpPort()));
    control.pubSubSocketPair.publisherSocketConnector()
        .bind(TransportProtocol.IPC, Connector.createAddress(parameter.getHost(), parameter.getControlPubTcpPort()));
    control.pubSubSocketPair.subscriberSocketConnector()
        .bind(TransportProtocol.IPC, Connector.createAddress(parameter.getHost(), parameter.getControlSubTcpPort()));
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
