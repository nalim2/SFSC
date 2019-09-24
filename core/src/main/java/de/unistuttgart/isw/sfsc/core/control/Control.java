package de.unistuttgart.isw.sfsc.core.control;

import de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager.InboxTopicManagerImpl;
import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.SubscriptionEventProcessor;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubSocketPair;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactiveinbox.ReactiveInbox;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ContextConfiguration;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import de.unistuttgart.isw.sfsc.core.hazelcast.Registry;
import java.util.concurrent.ExecutionException;

public class Control implements AutoCloseable {

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

    InboxTopicManagerImpl inboxTopicManager = new InboxTopicManagerImpl(pubSubConnection.subscriptionManager());
    inboxTopicManager.addTopicListener(sessionManager);
    inboxTopicManager.addTopicListener(registryManager);

    reactiveDataInbox = ReactiveInbox.create(pubSubConnection.dataInbox(), inboxTopicManager);
    reactiveSubscriptionInbox = ReactiveInbox.create(pubSubConnection.subEventInbox(), new SubscriptionEventProcessor(sessionManager));
  }

  public static Control create(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration, Registry registry)
      throws ExecutionException, InterruptedException {
    Control control = new Control(contextConfiguration, configuration, registry);
    control.pubSubSocketPair.publisherSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.CONTROL_PUB_PORT)));
    control.pubSubSocketPair.subscriberSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.CONTROL_SUB_PORT)));
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
