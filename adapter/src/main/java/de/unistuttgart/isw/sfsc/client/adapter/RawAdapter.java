package de.unistuttgart.isw.sfsc.client.adapter;

import de.unistuttgart.isw.sfsc.client.adapter.control.ControlClient;
import de.unistuttgart.isw.sfsc.client.adapter.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.concurrent.ExecutionException;
import zmq.pubsubsocketpair.PubSubSocketPair;
import zmq.pubsubsocketpair.SimplePubSubSocketPair;
import zmq.reactor.ContextConfiguration;
import zmq.reactor.Reactor;

public class RawAdapter implements AutoCloseable {

  private final Reactor reactor;
  private final ControlClient controlClient;
  private final SimplePubSubSocketPair dataPubSubSocketPair;

  RawAdapter(Reactor reactor, ControlClient controlClient, SimplePubSubSocketPair dataPubSubSocketPair) {
    this.reactor = reactor;
    this.controlClient = controlClient;
    this.dataPubSubSocketPair = dataPubSubSocketPair;
  }

  public static RawAdapter create(BootstrapConfiguration configuration) throws InterruptedException, ExecutionException {
    ContextConfiguration contextConfiguration = context -> {
      context.setLinger(0);
      context.setRcvHWM(0);
      context.setSndHWM(0);
    };
    Reactor reactor = Reactor.create(contextConfiguration);
    ControlClient controlClient = ControlClient.create(reactor, configuration);
    WelcomeMessage welcomeMessage = controlClient.welcomeMessage();

    SimplePubSubSocketPair dataPubSubSocketPair = SimplePubSubSocketPair.create(reactor);
    dataPubSubSocketPair.publisherSocketConnector().connect(welcomeMessage.getHost(), welcomeMessage.getDataSubPort());
    dataPubSubSocketPair.subscriberSocketConnector().connect(welcomeMessage.getHost(), welcomeMessage.getDataPubPort());

    return new RawAdapter(reactor, controlClient, dataPubSubSocketPair);
  }

  public RegistryClient registryClient() {
    return controlClient.registryClient();
  }

  public PubSubSocketPair dataClient() {
    return dataPubSubSocketPair;
  }

  @Override
  public void close() {
    reactor.close();
    dataPubSubSocketPair.close();
    controlClient.close();
  }
}
