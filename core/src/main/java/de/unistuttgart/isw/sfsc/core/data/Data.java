package de.unistuttgart.isw.sfsc.core.data;

import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import java.util.List;
import java.util.concurrent.ExecutionException;
import zmq.forwarder.Forwarder;
import zmq.pubsubsocketpair.SimplePubSubSocketPair;
import zmq.reactor.ContextConfiguration;
import zmq.reactor.Reactor;

public class Data implements AutoCloseable {

  private final SimplePubSubSocketPair frontend;
  private final SimplePubSubSocketPair backend;
  private final Configuration<CoreOption> configuration;
  private final Forwarder backendDataInboxForwarder;
  private final Forwarder backendSubEventInboxForwarder;
  private final Forwarder frontendDataInboxForwarder;
  private final Forwarder frontendSubEventInboxForwarder;
  private final Reactor reactor;

  Data(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration) throws ExecutionException, InterruptedException {
    this.configuration = configuration;
    reactor = Reactor.create(contextConfiguration);
    frontend = SimplePubSubSocketPair.create(reactor);
    backend = SimplePubSubSocketPair.create(reactor);
    backendDataInboxForwarder = Forwarder.create(backend.dataInbox(), frontend.publisher().outbox());
    backendSubEventInboxForwarder = Forwarder.create(backend.subEventInbox(), frontend.subscriptionManager().outbox());
    frontendDataInboxForwarder = Forwarder.create(frontend.dataInbox(), List.of(frontend.publisher().outbox(), backend.publisher().outbox()));
    frontendSubEventInboxForwarder = Forwarder.create(frontend.subEventInbox(), List.of(frontend.subscriptionManager().outbox(), backend.subscriptionManager().outbox()));
  }

  public static Data create(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration) throws ExecutionException, InterruptedException {
    Data data = new Data(contextConfiguration, configuration);
    data.bindFrontend(configuration);
    data.bindBackend(configuration);
    return data;
  }

  void bindFrontend(Configuration<CoreOption> configuration) {
    frontend.publisherSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.DATA_PUB_PORT)));
    frontend.subscriberSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.DATA_SUB_PORT)));
  }

  void bindBackend(Configuration<CoreOption> configuration) {
    backend.subscriberSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.BACKEND_PORT)));
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
    reactor.close();
    backendDataInboxForwarder.close();
    backendSubEventInboxForwarder.close();
    frontendDataInboxForwarder.close();
    frontendSubEventInboxForwarder.close();
    frontend.close();
    backend.close();
  }
}
