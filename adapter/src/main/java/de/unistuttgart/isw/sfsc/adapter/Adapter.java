package de.unistuttgart.isw.sfsc.adapter;

import de.unistuttgart.isw.sfsc.adapter.control.ControlClient;
import de.unistuttgart.isw.sfsc.adapter.control.registry.RegistryApi;
import de.unistuttgart.isw.sfsc.adapter.control.session.WelcomeInformation;
import de.unistuttgart.isw.sfsc.adapter.data.DataClient;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ContextConfiguration;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Adapter implements NotThrowingAutoCloseable {

  private final Reactor reactor;
  private final ControlClient controlClient;
  private final DataClient dataClient;
  private final AdapterInformation adapterInformation;

  Adapter(Reactor reactor, ControlClient controlClient, DataClient dataClient, AdapterInformation adapterInformation) {
    this.reactor = reactor;
    this.controlClient = controlClient;
    this.dataClient = dataClient;
    this.adapterInformation = adapterInformation;
  }

  public static Adapter create(BootstrapConfiguration configuration) throws InterruptedException, ExecutionException, TimeoutException {
    ContextConfiguration contextConfiguration = context -> {
      context.setRcvHWM(0);
      context.setSndHWM(0);
    };
    Reactor reactor = Reactor.create(contextConfiguration);
    ControlClient controlClient = ControlClient.create(reactor, configuration);
    WelcomeInformation welcomeInformation = controlClient.welcomeInformation();
    AdapterInformation adapterInformation = new AdapterInformation(welcomeInformation.getCoreId(), welcomeInformation.getAdapterId(),
        configuration.getCoreHost(), welcomeInformation.getCoreDataPubPort(), welcomeInformation.getCoreDataSubPort(),
        welcomeInformation.getCoreDataPubPort(), welcomeInformation.getCoreDataSubPort());
    DataClient dataClient = DataClient.create(reactor, adapterInformation);

    return new Adapter(reactor, controlClient, dataClient, adapterInformation);
  }

  public RegistryApi registryClient() {
    return controlClient.registryClient();
  }

  public PubSubConnection dataConnection() {
    return dataClient.pubSubConnection();
  }

  public AdapterInformation adapterInformation() {
    return adapterInformation;
  }

  @Override
  public void close() {
    reactor.close();
    controlClient.close();
    dataClient.close();
  }
}
