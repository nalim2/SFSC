package de.unistuttgart.isw.sfsc.client.adapter;

import de.unistuttgart.isw.sfsc.client.adapter.control.ControlClient;
import de.unistuttgart.isw.sfsc.client.adapter.data.DataClient;
import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import zmq.reactor.ContextConfiguration;
import zmq.reactor.Reactor;

public class Adapter implements AutoCloseable {

  private final Reactor reactor;
  private final ControlClient controlClient;
  private final DataClient dataClient;

  Adapter(Reactor reactor, ControlClient controlClient, DataClient dataClient) {
    this.reactor = reactor;
    this.controlClient = controlClient;
    this.dataClient = dataClient;
  }

 public static Adapter create(BootstrapConfiguration configuration) throws InterruptedException, ExecutionException {
   ContextConfiguration contextConfiguration = context -> {
     context.setLinger(0);
     context.setRcvHWM(0);
     context.setSndHWM(0);
   };
    Reactor reactor = Reactor.create(contextConfiguration);
    UUID uuid = UUID.randomUUID();
    CountDownLatch ready = new CountDownLatch(1);
    ControlClient controlClient = ControlClient.create(reactor, configuration, uuid, ready);
    DataClient dataClient = DataClient.create(reactor);
    Adapter adapter = new Adapter(reactor, controlClient, dataClient);
    WelcomeMessage welcomeMessage = controlClient.getWelcomeMessage().get();
    adapter.connect(welcomeMessage);
    ready.await();
    return adapter;
  }

  void connect(WelcomeMessage welcomeMessage) {
    controlClient.connectPubSocket(welcomeMessage);
    dataClient.connect(welcomeMessage);
  }

  public DataClient getDataClient() {
    return dataClient;
  }

  @Override
  public void close() throws Exception {
    reactor.close();
    dataClient.close();
    controlClient.close();
  }
}
