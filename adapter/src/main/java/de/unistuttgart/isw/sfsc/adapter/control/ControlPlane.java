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
import de.unistuttgart.isw.sfsc.adapter.data.DataParameter;
import de.unistuttgart.isw.sfsc.clientserver.protocol.bootstrap.BootstrapMessage;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Hello;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Welcome;
import de.unistuttgart.isw.sfsc.commonjava.heartbeating.HeartbeatModule;
import de.unistuttgart.isw.sfsc.commonjava.heartbeating.HeartbeatParameter;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.ListenableEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.SchedulerService;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnectionImplementation;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubSocketPair;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.TransportProtocol;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ControlPlane implements NotThrowingAutoCloseable {

  private final SchedulerService schedulerService = new SchedulerService(1);
  private final ListenableEvent coreLostEvent = new ListenableEvent();
  private final PubSubSocketPair pubSubSocketPair;
  private final PubSubConnectionImplementation pubSubConnection;

  private final HeartbeatModule heartbeatModule;
  private final RegistryModule registryModule;
  private final AdapterInformation adapterInformation;
  private final DataParameter dataParameter;

  public ControlPlane(Reactor reactor, AdapterParameter parameter)
      throws ExecutionException, InterruptedException, TimeoutException {

    BootstrapperParameter bootstrapperParameter = new BootstrapperParameter(parameter.getBootstrapCoreTopic(),
        parameter.getControlTimeoutMs());
    HandshakerParameter handshakerParameter = new HandshakerParameter(parameter.getHandshakeCoreTopic(),
        parameter.getHandshakeAdapterTopic(),
        parameter.getControlTimeoutMs());
    HeartbeatParameter heartbeatParameter = new HeartbeatParameter(parameter.getAdapterId(),
        parameter.getHeartbeatSendRateMs(),
        parameter.getHeartbeatAdapterTopic(),
        parameter.getHeartbeatDeadlineIncomingMs());
    RegistryParameter registryParameter = new RegistryParameter(parameter.getAdapterId(),
        parameter.getRegistryCoreQueryTopic(),
        parameter.getRegistryCoreCommandTopic(),
        parameter.getRegistryCoreEventTopic(),
        parameter.getRegistryAdapterQueryTopic(),
        parameter.getRegistryAdapterCommandTopic(),
        parameter.getControlTimeoutMs(),
        parameter.getRegistryPollingRateMs());

    pubSubSocketPair = PubSubSocketPair.create(reactor);
    pubSubConnection = PubSubConnectionImplementation.create(pubSubSocketPair);
    pubSubConnection.start();

    String coreControlPubAddress;
    if (parameter.getTransportProtocol() == TransportProtocol.TCP) {
      coreControlPubAddress = createAddress(parameter.getCoreHost(), parameter.getCorePubTcpPort());
    } else if (parameter.getTransportProtocol() == TransportProtocol.IPC) {
      coreControlPubAddress = new File(parameter.getCoreIpcLocation(), parameter.getCorePubIpcFile()).getAbsolutePath();
    } else {
      throw new IllegalArgumentException();
    }
    pubSubSocketPair.subscriberSocketConnector().connect(parameter.getTransportProtocol(), coreControlPubAddress);

    BootstrapMessage bootstrapMessage = Bootstrapper.bootstrap(bootstrapperParameter,
        pubSubConnection,
        schedulerService);

    String coreControlSubAddress;
    if (parameter.getTransportProtocol() == TransportProtocol.TCP) {
      coreControlSubAddress = createAddress(parameter.getCoreHost(), bootstrapMessage.getCoreControlSubTcpPort());
    } else if (parameter.getTransportProtocol() == TransportProtocol.IPC) {
      coreControlSubAddress = new File(parameter.getCoreIpcLocation(),
          bootstrapMessage.getCoreControlSubIpcFile()).getAbsolutePath();
    } else {
      throw new IllegalArgumentException();
    }
    pubSubSocketPair.publisherSocketConnector().connect(parameter.getTransportProtocol(), coreControlSubAddress);

    Hello hello = Hello
        .newBuilder()
        .setAdapterId(heartbeatParameter.getOutgoingId())
        .setHeartbeatTopic(heartbeatParameter.getExpectedIncomingTopic())
        .build();
    Welcome welcome = Handshaker.handshake(handshakerParameter, pubSubConnection, schedulerService, hello);

    heartbeatModule = HeartbeatModule.create(pubSubConnection, schedulerService, heartbeatParameter);
    ByteString heartbeatCoreTopic = ByteString.copyFromUtf8(parameter.getHeartbeatCoreTopic());
    heartbeatModule.startSession(welcome.getCoreId(), heartbeatCoreTopic, coreId -> coreLostEvent.fire());
    registryModule = RegistryModule.create(registryParameter, pubSubConnection, schedulerService);

    adapterInformation = new AdapterInformation(welcome.getCoreId(),
        parameter.getAdapterId(),
        parameter.getTransportProtocol());

    String coreDataPubAddress;
    if (parameter.getTransportProtocol() == TransportProtocol.TCP) {
      coreDataPubAddress = createAddress(parameter.getCoreHost(), bootstrapMessage.getCoreDataPubTcpPort());
    } else if (parameter.getTransportProtocol() == TransportProtocol.IPC) {
      coreDataPubAddress = new File(parameter.getCoreIpcLocation(),
          bootstrapMessage.getCoreDataPubIpcFile()).getAbsolutePath();
    } else {
      throw new IllegalArgumentException();
    }
    String coreDataSubAddress;
    if (parameter.getTransportProtocol() == TransportProtocol.TCP) {
      coreDataSubAddress = createAddress(parameter.getCoreHost(), bootstrapMessage.getCoreDataSubTcpPort());
    } else if (parameter.getTransportProtocol() == TransportProtocol.IPC) {
      coreDataSubAddress = new File(parameter.getCoreIpcLocation(),
          bootstrapMessage.getCoreDataSubIpcFile()).getAbsolutePath();
    } else {
      throw new IllegalArgumentException();
    }
    dataParameter = new DataParameter(parameter.getTransportProtocol(), coreDataPubAddress, coreDataSubAddress);
  }

  public Handle addCoreLostEventListener(Runnable runnable) {
    return coreLostEvent.addListener(runnable);
  }

  public DataParameter dataParameter() {
    return dataParameter;
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
    schedulerService.close();
  }

}
