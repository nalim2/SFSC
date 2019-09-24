package de.unistuttgart.isw.sfsc.adapter.base;

import de.unistuttgart.isw.sfsc.adapter.base.control.ControlClient;
import de.unistuttgart.isw.sfsc.adapter.base.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubSocketPair;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ContextConfiguration;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import de.unistuttgart.isw.sfsc.protocol.session.WelcomeMessage;
import java.util.concurrent.ExecutionException;

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

  public String coreId(){
    return controlClient.welcomeMessage().getCoreId();
  }

  public String adapterId(){
    return controlClient.welcomeMessage().getAdapterId();
  }

  @Override
  public void close() {
    reactor.close();
    dataPubSubSocketPair.close();
    controlClient.close();
  }
}
