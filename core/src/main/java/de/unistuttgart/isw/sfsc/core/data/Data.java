package de.unistuttgart.isw.sfsc.core.data;

import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.Forwarder;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubSocketPair;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactiveinbox.ReactiveInbox;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ContextConfiguration;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Data implements AutoCloseable {

  private final PubSubSocketPair frontend;
  private final PubSubSocketPair backend;
  private final Configuration<CoreOption> configuration;
  private final ReactiveInbox backendDataInbox;
  private final ReactiveInbox backendSubEventInbox;
  private final ReactiveInbox frontendDataInbox;
  private final ReactiveInbox frontendSubEventInbox;
  private final Reactor reactor;

  Data(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration) throws ExecutionException, InterruptedException {
    this.configuration = configuration;
    reactor = Reactor.create(contextConfiguration);
    frontend = PubSubSocketPair.create(reactor);
    backend = PubSubSocketPair.create(reactor);
    PubSubConnection frontendConnection = frontend.connection();
    PubSubConnection backendConnection = backend.connection();

    backendDataInbox = ReactiveInbox.create(backendConnection.dataInbox(),
        new Forwarder(frontendConnection.publisher().outbox()));
    backendSubEventInbox = ReactiveInbox.create(backendConnection.subEventInbox(),
        new Forwarder(frontendConnection.subscriptionManager().outbox()));
    frontendDataInbox = ReactiveInbox.create(frontendConnection.dataInbox(),
        new Forwarder(List.of(frontendConnection.publisher().outbox(), backendConnection.publisher().outbox())));
    frontendSubEventInbox = ReactiveInbox.create(frontendConnection.subEventInbox(),
        new Forwarder(List.of(frontendConnection.subscriptionManager().outbox(), backendConnection.subscriptionManager().outbox())));
  }

  public static Data create(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration)
      throws ExecutionException, InterruptedException {
    Data data = new Data(contextConfiguration, configuration);
    data.frontend.publisherSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.DATA_PUB_PORT)));
    data.frontend.subscriberSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.DATA_SUB_PORT)));
    data.backend.subscriberSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.BACKEND_PORT)));
    return data;
  }

  public void connectBackend(String host, int port) {
    if (!configuration.get(CoreOption.HOST).equals(host) && !configuration.get(CoreOption.BACKEND_PORT).equals(String.valueOf(port))) {
      backend.publisherSocketConnector().connect(host, port);
    }
  }

  public void disconnectBackend(String host, int port) {
    if (!configuration.get(CoreOption.HOST).equals(host) && !configuration.get(CoreOption.BACKEND_PORT).equals(String.valueOf(port))) {
      backend.publisherSocketConnector().disconnect(host, port);
    }
  }

  @Override
  public void close() {
    backendDataInbox.close();
    backendSubEventInbox.close();
    frontendDataInbox.close();
    frontendSubEventInbox.close();
    reactor.close();
    frontend.close();
    backend.close();
  }
}
