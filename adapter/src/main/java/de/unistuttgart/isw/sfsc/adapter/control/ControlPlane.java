package de.unistuttgart.isw.sfsc.adapter.control;

import static de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Connector.createAddress;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.adapter.AdapterInformation;
import de.unistuttgart.isw.sfsc.adapter.AdapterParameter;
import de.unistuttgart.isw.sfsc.adapter.control.bootstrapping.Bootstrapper;
import de.unistuttgart.isw.sfsc.adapter.control.bootstrapping.BootstrapperParameter;
import de.unistuttgart.isw.sfsc.adapter.control.handshake.Handshaker;
import de.unistuttgart.isw.sfsc.adapter.control.handshake.HandshakerParameter;
import de.unistuttgart.isw.sfsc.adapter.control.registry.RegistryModule;
import de.unistuttgart.isw.sfsc.adapter.control.registry.RegistryParameter;
import de.unistuttgart.isw.sfsc.clientserver.protocol.bootstrap.BootstrapMessage;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Hello;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Welcome;
import de.unistuttgart.isw.sfsc.commonjava.heartbeating.HeartbeatModule;
import de.unistuttgart.isw.sfsc.commonjava.heartbeating.HeartbeatParameter;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnectionImplementation;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubSocketPair;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.TransportProtocol;
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

  public ControlPlane(Reactor reactor, AdapterParameter parameter) throws ExecutionException, InterruptedException, TimeoutException {

    BootstrapperParameter bootstrapperParameter = new BootstrapperParameter(parameter.getBootstrapCoreTopic(), parameter.getControlTimeoutMs());
    HandshakerParameter handshakerParameter = new HandshakerParameter(parameter.getHandshakeCoreTopic(), parameter.getHandshakeAdapterTopic(),
        parameter.getControlTimeoutMs());
    HeartbeatParameter heartbeatParameter = new HeartbeatParameter(parameter.getAdapterId(), parameter.getHeartbeatSendRateMs(),
        parameter.getHeartbeatAdapterTopic(), parameter.getHeartbeatDeadlineIncomingMs());
    RegistryParameter registryParameter = new RegistryParameter(parameter.getAdapterId(), parameter.getRegistryCoreQueryTopic(),
        parameter.getRegistryCoreCommandTopic(), parameter.getRegistryCoreEventTopic(), parameter.getRegistryAdapterQueryTopic(),
        parameter.getRegistryAdapterCommandTopic(), parameter.getControlTimeoutMs(), parameter.getRegistryPollingRateMs());

    pubSubSocketPair = PubSubSocketPair.create(reactor);
    pubSubConnection = PubSubConnectionImplementation.create(pubSubSocketPair);
    pubSubConnection.start();

    pubSubSocketPair.subscriberSocketConnector()
        .connect(TransportProtocol.TCP, createAddress(parameter.getCoreHost(), parameter.getCorePort())); //todo connection type
    BootstrapMessage bootstrapMessage = Bootstrapper.bootstrap(bootstrapperParameter, pubSubConnection, executorService);
    pubSubSocketPair.publisherSocketConnector()
        .connect(TransportProtocol.TCP, createAddress(parameter.getCoreHost(), bootstrapMessage.getCoreSubscriptionPort()));//todo connection type

    Hello hello = Hello.newBuilder().setAdapterId(heartbeatParameter.getOutgoingId()).setHeartbeatTopic(heartbeatParameter.getExpectedIncomingTopic()).build();
    Welcome welcome = Handshaker.handshake(handshakerParameter, pubSubConnection, executorService, hello);

    heartbeatModule = HeartbeatModule.create(pubSubConnection, executorService, heartbeatParameter);
    ByteString heartbeatCoreTopic = ByteString.copyFromUtf8(parameter.getHeartbeatCoreTopic());
    heartbeatModule.startSession(welcome.getCoreId(), heartbeatCoreTopic, core -> System.out.println("Core died"));//todo
    registryModule = RegistryModule.create(registryParameter, pubSubConnection, executorService);

    adapterInformation = new AdapterInformation(welcome.getCoreId(), parameter.getAdapterId(),
        parameter.getCoreHost(), parameter.getCorePort(), bootstrapMessage.getCoreSubscriptionPort(),
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
