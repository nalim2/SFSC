package de.unistuttgart.isw.sfsc.client.adapter;

import de.unistuttgart.isw.sfsc.client.adapter.control.SimpleControlClient;
import de.unistuttgart.isw.sfsc.client.adapter.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.client.adapter.data.DataClient;
import de.unistuttgart.isw.sfsc.client.adapter.data.SimpleDataClient;
import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.concurrent.ExecutionException;
import zmq.reactor.ContextConfiguration;
import zmq.reactor.Reactor;

public class RawAdapter implements AutoCloseable {

  private final Reactor reactor;
  private final SimpleControlClient controlClient;
  private final SimpleDataClient dataClient;

  RawAdapter(Reactor reactor, SimpleControlClient controlClient, SimpleDataClient dataClient) {
    this.reactor = reactor;
    this.controlClient = controlClient;
    this.dataClient = dataClient;
  }

  public static RawAdapter create(BootstrapConfiguration configuration) throws InterruptedException, ExecutionException {
    ContextConfiguration contextConfiguration = context -> {
      context.setLinger(0);
      context.setRcvHWM(0);
      context.setSndHWM(0);
    };
    Reactor reactor = Reactor.create(contextConfiguration);
    SimpleControlClient controlClient = SimpleControlClient.create(reactor, configuration);
    WelcomeMessage welcomeMessage = controlClient.welcomeMessage();
    SimpleDataClient dataClient = SimpleDataClient.create(reactor, welcomeMessage);
    return new RawAdapter(reactor, controlClient, dataClient);
  }

  public RegistryClient registryClient() {
    return controlClient.registryClient();
  }

  public DataClient dataClient() {
    return dataClient;
  }

  @Override
  public void close() {
    reactor.close();
    dataClient.close();
    controlClient.close();
  }
}
