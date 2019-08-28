package de.unistuttgart.isw.sfsc.core.control;

import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import de.unistuttgart.isw.sfsc.core.hazelcast.Registry;
import java.util.concurrent.ExecutionException;
import zmq.processors.Forwarder;
import zmq.processors.MessageDistributor;
import zmq.pubsubsocketpair.PubSubSocketPair;
import zmq.pubsubsocketpair.SimplePubSubSocketPair;
import zmq.reactiveinbox.ReactiveInbox;
import zmq.reactor.ContextConfiguration;
import zmq.reactor.Reactor;

public class Control implements AutoCloseable {

  private final SimplePubSubSocketPair pubSubSocketPair;
  private final Reactor reactor;
  private final ReactiveInbox reactiveDataInbox;
  private final ReactiveInbox reactiveSubscriptionInbox;

  Control(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration, Registry registry) throws ExecutionException, InterruptedException {
    reactor = Reactor.create(contextConfiguration);
    pubSubSocketPair = SimplePubSubSocketPair.create(reactor);
    MessageDistributor messageDistributor = new MessageDistributor();
    messageDistributor.add(new RegistryEventProcessor(pubSubSocketPair.publisher(), registry));
    reactiveDataInbox = ReactiveInbox.create(pubSubSocketPair.dataInbox(), messageDistributor);

    pubSubSocketPair.subscriptionManager().subscribe("registry".getBytes()); //todo extract var
    pubSubSocketPair.subscriptionManager().subscribe("session".getBytes()); //todo extract var


    reactiveSubscriptionInbox = ReactiveInbox.create(pubSubSocketPair.subEventInbox(),
        new Forwarder(pubSubSocketPair.subscriptionManager().outbox())
            .andThen(new SubscriptionEventProcessor(pubSubSocketPair.publisher(), new SessionManager(configuration))));
  }

  public static Control create(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration, Registry registry)
      throws ExecutionException, InterruptedException {
    Control control = new Control(contextConfiguration, configuration, registry);
    bind(control.pubSubSocketPair, configuration);
    return control;
  }

  static void bind(PubSubSocketPair pubSubSocketPair, Configuration<CoreOption> configuration) {
    pubSubSocketPair.publisherSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.CONTROL_PUB_PORT)));
    pubSubSocketPair.subscriberSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.CONTROL_SUB_PORT)));
  }

  @Override
  public void close() {
    reactor.close();
    pubSubSocketPair.close();
    reactiveSubscriptionInbox.close();
    reactiveDataInbox.close();
  }
}
