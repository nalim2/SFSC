package de.unistuttgart.isw.sfsc.core.data;

import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import java.util.List;
import java.util.concurrent.ExecutionException;
import protocol.pubsub.DataProtocol;
import zmq.forwarder.Forwarder;
import zmq.pubsubsocketpair.PubSubSocketPair;
import zmq.reactor.ContextConfiguration;
import zmq.reactor.Reactor;

public class Data implements AutoCloseable {

  private final PubSubSocketPair frontend;
  private final PubSubSocketPair backend;
  private final Configuration<CoreOption> configuration;
  private final Forwarder backendDataInboxForwarder;
  private final Forwarder backendSubEventInboxForwarder;
  private final Forwarder frontendDataInboxForwarder;
  private final Forwarder frontendSubEventInboxForwarder;
  private final Reactor reactor;

  Data(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration) throws ExecutionException, InterruptedException {
    this.configuration = configuration;
    reactor = Reactor.create(contextConfiguration);
    frontend = PubSubSocketPair.create(reactor, DataProtocol.class);
    backend = PubSubSocketPair.create(reactor, DataProtocol.class);
    backendDataInboxForwarder = Forwarder.create(backend.getDataInbox(), frontend.getDataOutbox());
    backendSubEventInboxForwarder = Forwarder.create(backend.getSubEventInbox(), frontend.getSubEventOutbox());
    frontendDataInboxForwarder = Forwarder.create(frontend.getDataInbox(), List.of(frontend.getDataOutbox(), backend.getDataOutbox()));
    frontendSubEventInboxForwarder = Forwarder.create(frontend.getSubEventInbox(), List.of(frontend.getSubEventOutbox(), backend.getSubEventOutbox()));
  }

  public static Data create(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration) throws ExecutionException, InterruptedException {
    Data data = new Data(contextConfiguration, configuration);
    data.bindFrontend(configuration);
    data.bindBackend(configuration);
    return data;
  }

  void bindFrontend(Configuration<CoreOption> configuration) {
    frontend.getPublisherSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.DATA_PUB_PORT)));
    frontend.getSubscriberSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.DATA_SUB_PORT)));
  }

  void bindBackend(Configuration<CoreOption> configuration) {
    backend.getSubscriberSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.BACKEND_PORT)));
  }

  public void connectBackend(String host, int port) {
    if (!configuration.get(CoreOption.HOST).equals(host) && !configuration.get(CoreOption.BACKEND_PORT).equals(String.valueOf(port))) {
      backend.getPublisherSocketConnector().connect(host, port);
    }
  }

  public void disconnectBackend(String host, int port) {
    if (!configuration.get(CoreOption.HOST).equals(host) && !configuration.get(CoreOption.BACKEND_PORT).equals(String.valueOf(port))) {
      backend.getPublisherSocketConnector().disconnect(host, port);
    }
  }

  @Override
  public void close() throws Exception {
    reactor.close();
    backendDataInboxForwarder.close();
    backendSubEventInboxForwarder.close();
    frontendDataInboxForwarder.close();
    frontendSubEventInboxForwarder.close();
    frontend.close();
    backend.close();
  }
}
