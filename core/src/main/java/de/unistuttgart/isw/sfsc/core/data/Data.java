package de.unistuttgart.isw.sfsc.core.data;

import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import java.util.List;
import java.util.concurrent.ExecutionException;
import zmq.processors.Forwarder;
import zmq.pubsubsocketpair.SimplePubSubSocketPair;
import zmq.reactiveinbox.ReactiveInbox;
import zmq.reactor.ContextConfiguration;
import zmq.reactor.Reactor;

public class Data implements AutoCloseable {

  private final SimplePubSubSocketPair frontend;
  private final SimplePubSubSocketPair backend;
  private final Configuration<CoreOption> configuration;
  private final ReactiveInbox backendDataInbox;
  private final ReactiveInbox backendSubEventInbox;
  private final ReactiveInbox frontendDataInbox;
  private final ReactiveInbox frontendSubEventInbox;
  private final Reactor reactor;

  Data(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration) throws ExecutionException, InterruptedException {
    this.configuration = configuration;
    reactor = Reactor.create(contextConfiguration);
    frontend = SimplePubSubSocketPair.create(reactor);
    backend = SimplePubSubSocketPair.create(reactor);
    backendDataInbox = ReactiveInbox.create(backend.dataInbox(), new Forwarder(frontend.publisher().outbox()));
    backendSubEventInbox = ReactiveInbox.create(backend.subEventInbox(), new Forwarder(frontend.subscriptionManager().outbox()));
    frontendDataInbox = ReactiveInbox.create(frontend.dataInbox(), new Forwarder(List.of(frontend.publisher().outbox(), backend.publisher().outbox())));
    frontendSubEventInbox = ReactiveInbox.create(frontend.subEventInbox(), new Forwarder(List.of(frontend.subscriptionManager().outbox(), backend.subscriptionManager().outbox())));
  }

  public static Data create(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration) throws ExecutionException, InterruptedException {
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
