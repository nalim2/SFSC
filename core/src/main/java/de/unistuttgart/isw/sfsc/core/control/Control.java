package de.unistuttgart.isw.sfsc.core.control;

import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import de.unistuttgart.isw.sfsc.core.hazelcast.Registry;
import java.util.concurrent.ExecutionException;
import zmq.processors.Forwarder;
import zmq.processors.MessageDistributor;
import zmq.processors.SubscriptionEventProcessor;
import zmq.pubsubsocketpair.PubSubConnection;
import zmq.pubsubsocketpair.PubSubSocketPair;
import zmq.reactiveinbox.ReactiveInbox;
import zmq.reactor.ContextConfiguration;
import zmq.reactor.Reactor;

public class Control implements AutoCloseable {

  private static final String REGISTRY_BASE_TOPIC = "registry";
  private static final String SESSION_BASE_TOPIC = "session";

  private final PubSubSocketPair pubSubSocketPair;
  private final Reactor reactor;
  private final ReactiveInbox reactiveDataInbox;
  private final ReactiveInbox reactiveSubscriptionInbox;

  Control(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration, Registry registry)
      throws ExecutionException, InterruptedException {
    reactor = Reactor.create(contextConfiguration);
    pubSubSocketPair = PubSubSocketPair.create(reactor);
    PubSubConnection pubSubConnection = pubSubSocketPair.connection();

    SessionManager sessionManager = new SessionManager(configuration, pubSubConnection.publisher());
    RegistryManager registryManager = new RegistryManager(pubSubConnection.publisher(), registry);

    MessageDistributor messageDistributor = new MessageDistributor();
    messageDistributor.add(sessionManager);
    messageDistributor.add(registryManager);

    pubSubConnection.subscriptionManager().subscribe(SessionManager.TOPIC);
    pubSubConnection.subscriptionManager().subscribe(RegistryManager.TOPIC);

    reactiveDataInbox = ReactiveInbox.create(pubSubConnection.dataInbox(), messageDistributor);
    reactiveSubscriptionInbox = ReactiveInbox.create(pubSubConnection.subEventInbox(),
        new Forwarder(pubSubConnection.subscriptionManager().outbox())
            .andThen(new SubscriptionEventProcessor(sessionManager)));
  }

  public static Control create(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration, Registry registry)
      throws ExecutionException, InterruptedException {
    Control control = new Control(contextConfiguration, configuration, registry);
    control.pubSubSocketPair.publisherSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.CONTROL_PUB_PORT)));
    control.pubSubSocketPair.subscriberSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.CONTROL_SUB_PORT)));
    control.pubSubSocketPair.connection().subscriptionManager().subscribe(REGISTRY_BASE_TOPIC);
    control.pubSubSocketPair.connection().subscriptionManager().subscribe(SESSION_BASE_TOPIC);
    return control;
  }

  @Override
  public void close() {
    reactor.close();
    pubSubSocketPair.close();
    reactiveSubscriptionInbox.close();
    reactiveDataInbox.close();
  }
}
