package de.unistuttgart.isw.sfsc.core.data;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubSocketPair;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.forwarder.ForwardingInbox;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ContextConfiguration;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactorFactory;
import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import java.util.concurrent.ExecutionException;

public class Data implements NotThrowingAutoCloseable {

  private final PubSubSocketPair frontend;
  private final PubSubSocketPair backend;
  private final Configuration<CoreOption> configuration;
  private final ForwardingInbox backendDataInbox;
  private final ForwardingInbox backendSubscriptionInbox;
  private final ForwardingInbox frontendDataInbox;
  private final ForwardingInbox frontendSubscriptionInbox;
  private final Reactor reactor;

  Data(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration) throws ExecutionException, InterruptedException {
    this.configuration = configuration;
    reactor = ReactorFactory.create(contextConfiguration);
    frontend = PubSubSocketPair.create(reactor);
    backend = PubSubSocketPair.create(reactor);

    backendDataInbox = ForwardingInbox.create(backend.dataInbox());
    backendSubscriptionInbox = ForwardingInbox.create(backend.subscriptionInbox());
    frontendDataInbox = ForwardingInbox.create(frontend.dataInbox());
    frontendSubscriptionInbox = ForwardingInbox.create(frontend.subscriptionInbox());

    backendDataInbox.addListener(frontend.dataOutbox()::add);
    backendSubscriptionInbox.addListener(frontend.subscriptionOutbox()::add);
    frontendDataInbox.addListener(backend.dataOutbox()::add);
    frontendDataInbox.addListener(frontend.dataOutbox()::add);
    frontendSubscriptionInbox.addListener(backend.subscriptionOutbox()::add);
    frontendSubscriptionInbox.addListener(frontend.subscriptionOutbox()::add);

    backendDataInbox.start();
    backendSubscriptionInbox.start();
    frontendDataInbox.start();
    frontendSubscriptionInbox.start();
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
    if (!configuration.get(CoreOption.HOST).equals(host) || !configuration.get(CoreOption.BACKEND_PORT).equals(String.valueOf(port))) {
      backend.publisherSocketConnector().connect(host, port);
    }
  }

  public void disconnectBackend(String host, int port) {
    if (!configuration.get(CoreOption.HOST).equals(host) || !configuration.get(CoreOption.BACKEND_PORT).equals(String.valueOf(port))) {
      backend.publisherSocketConnector().disconnect(host, port);
    }
  }

  @Override
  public void close() {
    backendDataInbox.close();
    backendSubscriptionInbox.close();
    frontendDataInbox.close();
    frontendSubscriptionInbox.close();
    reactor.close();
    frontend.close();
    backend.close();
  }
}
