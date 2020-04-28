package de.unistuttgart.isw.sfsc.adapter;

import de.unistuttgart.isw.sfsc.adapter.configuration.AdapterConfiguration;
import de.unistuttgart.isw.sfsc.adapter.control.ControlPlane;
import de.unistuttgart.isw.sfsc.adapter.control.RegistryApi;
import de.unistuttgart.isw.sfsc.adapter.data.DataPlane;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactorFactory;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Adapter implements NotThrowingAutoCloseable {

  private final Reactor reactor;
  private final ControlPlane controlPlane;
  private final DataPlane dataPlane;

  Adapter(Reactor reactor, ControlPlane controlPlane, DataPlane dataPlane) {
    this.reactor = reactor;
    this.controlPlane = controlPlane;
    this.dataPlane = dataPlane;
  }

  public static Adapter create() throws InterruptedException, ExecutionException, TimeoutException {
    return create(new AdapterConfiguration());
  }

  public static Adapter create(AdapterConfiguration configuration) throws InterruptedException, ExecutionException, TimeoutException {
    AdapterParameter parameter = configuration.toAdapterParameter();
    Reactor reactor = ReactorFactory.create();
    ControlPlane controlPlane = new ControlPlane(reactor, parameter);
    DataPlane dataPlane = new DataPlane(reactor, controlPlane.adapterInformation());
    return new Adapter(reactor, controlPlane, dataPlane);
  }

  public RegistryApi registryClient() {
    return controlPlane.registryClient();
  }

  public PubSubConnection dataConnection() {
    return dataPlane.pubSubConnection();
  }

  public AdapterInformation adapterInformation() {
    return controlPlane.adapterInformation();
  }

  @Override
  public void close() {
    reactor.close();
    controlPlane.close();
    dataPlane.close();
  }
}
