package de.unistuttgart.isw.sfsc.core.control;

import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import de.unistuttgart.isw.sfsc.core.hazelcast.Registry;
import java.util.concurrent.ExecutionException;
import zmq.processors.Forwarder;
import zmq.processors.MessageDistributor;
import zmq.processors.SubscriptionEventProcessor;
import zmq.pubsubsocketpair.SimplePubSubSocketPair;
import zmq.reactiveinbox.ReactiveInbox;
import zmq.reactor.ContextConfiguration;
import zmq.reactor.Reactor;

public class Control implements AutoCloseable {

  private static final String REGISTRY_BASE_TOPIC = "registry";
  private static final String SESSION_BASE_TOPIC = "session";

  private final SimplePubSubSocketPair pubSubSocketPair;
  private final Reactor reactor;
  private final ReactiveInbox reactiveDataInbox;
  private final ReactiveInbox reactiveSubscriptionInbox;

  Control(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration, Registry registry)
      throws ExecutionException, InterruptedException {
    reactor = Reactor.create(contextConfiguration);
    pubSubSocketPair = SimplePubSubSocketPair.create(reactor);
    MessageDistributor messageDistributor = new MessageDistributor();
    messageDistributor.add(new RegistryEventProcessor(pubSubSocketPair.publisher(), registry));
    reactiveDataInbox = ReactiveInbox.create(pubSubSocketPair.dataInbox(), messageDistributor);
    reactiveSubscriptionInbox = ReactiveInbox.create(pubSubSocketPair.subEventInbox(),
        new Forwarder(pubSubSocketPair.subscriptionManager().outbox())
            .andThen(new SubscriptionEventProcessor(new SessionManager(configuration, pubSubSocketPair.publisher()))));
  }

  public static Control create(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration, Registry registry)
      throws ExecutionException, InterruptedException {
    Control control = new Control(contextConfiguration, configuration, registry);
    control.pubSubSocketPair.publisherSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.CONTROL_PUB_PORT)));
    control.pubSubSocketPair.subscriberSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.CONTROL_SUB_PORT)));
    control.pubSubSocketPair.subscriptionManager().subscribe(REGISTRY_BASE_TOPIC);
    control.pubSubSocketPair.subscriptionManager().subscribe(SESSION_BASE_TOPIC);
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
