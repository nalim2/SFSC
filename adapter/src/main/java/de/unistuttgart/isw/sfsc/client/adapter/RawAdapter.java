package de.unistuttgart.isw.sfsc.client.adapter;

import de.unistuttgart.isw.sfsc.client.adapter.control.ControlClient;
import de.unistuttgart.isw.sfsc.client.adapter.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.concurrent.ExecutionException;
import zmq.pubsubsocketpair.PubSubConnection;
import zmq.pubsubsocketpair.PubSubSocketPair;
import zmq.reactor.ContextConfiguration;
import zmq.reactor.Reactor;

public class RawAdapter implements AutoCloseable {

  private final Reactor reactor;
  private final ControlClient controlClient;
  private final PubSubSocketPair dataPubSubSocketPair;

  RawAdapter(Reactor reactor, ControlClient controlClient, PubSubSocketPair dataPubSubSocketPair) {
    this.reactor = reactor;
    this.controlClient = controlClient;
    this.dataPubSubSocketPair = dataPubSubSocketPair;
  }

  public static RawAdapter create(BootstrapConfiguration configuration) throws InterruptedException, ExecutionException {
    ContextConfiguration contextConfiguration = context -> {
      context.setRcvHWM(0);
      context.setSndHWM(0);
    };
    Reactor reactor = Reactor.create(contextConfiguration);
    ControlClient controlClient = ControlClient.create(reactor, configuration);
    WelcomeMessage welcomeMessage = controlClient.welcomeMessage();

    PubSubSocketPair dataPubSubSocketPair = PubSubSocketPair.create(reactor);
    dataPubSubSocketPair.publisherSocketConnector().connect(welcomeMessage.getHost(), welcomeMessage.getDataSubPort());
    dataPubSubSocketPair.subscriberSocketConnector().connect(welcomeMessage.getHost(), welcomeMessage.getDataPubPort());

    return new RawAdapter(reactor, controlClient, dataPubSubSocketPair);
  }

  public RegistryClient registryClient() {
    return controlClient.registryClient();
  }

  public PubSubConnection dataConnection() {
    return dataPubSubSocketPair.connection();
  }

  @Override
  public void close() {
    reactor.close();
    dataPubSubSocketPair.close();
    controlClient.close();
  }
}
