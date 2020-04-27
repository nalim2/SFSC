package de.unistuttgart.isw.sfsc.adapter.control;

import static de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Connector.createAddress;

import de.unistuttgart.isw.sfsc.adapter.AdapterInformation;
import de.unistuttgart.isw.sfsc.adapter.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.adapter.control.bootstrapping.Bootstrapper;
import de.unistuttgart.isw.sfsc.adapter.control.configuration.BootstrapperConfiguration;
import de.unistuttgart.isw.sfsc.adapter.control.configuration.HandshakerConfiguration;
import de.unistuttgart.isw.sfsc.adapter.control.configuration.HeartbeatConfiguration;
import de.unistuttgart.isw.sfsc.adapter.control.configuration.RegistryConfiguration;
import de.unistuttgart.isw.sfsc.adapter.control.handshake.Handshaker;
import de.unistuttgart.isw.sfsc.adapter.control.registry.RegistryModule;
import de.unistuttgart.isw.sfsc.clientserver.protocol.bootstrap.BootstrapMessage;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Hello;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Welcome;
import de.unistuttgart.isw.sfsc.commonjava.heartbeating.HeartbeatModule;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnectionImplementation;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubSocketPair;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.TransportProtocol;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

public class ControlPlane implements NotThrowingAutoCloseable {

  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  private final PubSubSocketPair pubSubSocketPair;
  private final PubSubConnectionImplementation pubSubConnection;

  private final HeartbeatModule heartbeatModule;
  private final RegistryModule registryModule;
  private final AdapterInformation adapterInformation;

  public ControlPlane(Reactor reactor, BootstrapConfiguration configuration) throws ExecutionException, InterruptedException, TimeoutException {
    String adapterId = UUID.randomUUID().toString();
    BootstrapperConfiguration bootstrapperConfiguration = new BootstrapperConfiguration();
    HandshakerConfiguration handshakerConfiguration = new HandshakerConfiguration(adapterId);
    HeartbeatConfiguration heartbeatConfiguration = new HeartbeatConfiguration(adapterId);
    RegistryConfiguration registryConfiguration = new RegistryConfiguration(adapterId);

    pubSubSocketPair = PubSubSocketPair.create(reactor);
    pubSubConnection = PubSubConnectionImplementation.create(pubSubSocketPair);
    pubSubConnection.start();

    pubSubSocketPair.subscriberSocketConnector()
        .connect(TransportProtocol.TCP, createAddress(configuration.getCoreHost(), configuration.getCorePort())); //todo connection type
    BootstrapMessage bootstrapMessage = Bootstrapper.bootstrap(bootstrapperConfiguration.toParameter(), pubSubConnection, executorService);
    pubSubSocketPair.publisherSocketConnector()
        .connect(TransportProtocol.TCP, createAddress(configuration.getCoreHost(), bootstrapMessage.getCoreSubscriptionPort()));//todo connection type

    Hello hello = Hello.newBuilder().setAdapterId(adapterId).setHeartbeatTopic(heartbeatConfiguration.getAdapterTopic()).build();
    Welcome welcome = Handshaker.handshake(handshakerConfiguration.toParameter(), pubSubConnection, executorService, hello);

    heartbeatModule = HeartbeatModule.create(pubSubConnection, executorService, heartbeatConfiguration.toParameter());
    heartbeatModule.startSession(welcome.getCoreId(), heartbeatConfiguration.getCoreTopic(), core -> System.out.println("Core died"));//todo
    registryModule = RegistryModule.create(registryConfiguration.toParameter(), pubSubConnection, executorService);

    adapterInformation = new AdapterInformation(welcome.getCoreId(), adapterId,
        configuration.getCoreHost(), configuration.getCorePort(), bootstrapMessage.getCoreSubscriptionPort(),
        welcome.getDataPubPort(), welcome.getDataSubPort());
  }

  public AdapterInformation adapterInformation() {
    return adapterInformation;
  }

  public RegistryApi registryClient() {
    return registryModule;
  }

  @Override
  public void close() {
    pubSubSocketPair.close();
    pubSubConnection.close();
    registryModule.close();
    heartbeatModule.close();
    executorService.shutdownNow();
  }

}
