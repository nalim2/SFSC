package de.unistuttgart.isw.sfsc.client.adapter.control;

import de.unistuttgart.isw.sfsc.client.adapter.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.client.adapter.control.registry.AdapterRegistryClient;
import de.unistuttgart.isw.sfsc.client.adapter.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.client.adapter.control.session.SessionManager;
import de.unistuttgart.isw.sfsc.client.adapter.control.session.SimpleSessionManager;
import de.unistuttgart.isw.sfsc.commonjava.zmq.highlevelinbox.HighLevelInbox;
import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.SubscriptionEventProcessor;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubSocketPair;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactiveinbox.ReactiveInbox;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import de.unistuttgart.isw.sfsc.protocol.session.WelcomeMessage;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ControlClient implements AutoCloseable {

  private final PubSubSocketPair pubSubSocketPair;
  private final SessionManager sessionManager;
  private final ReactiveInbox reactiveDataInbox;
  private final ReactiveInbox reactiveSubscriptionInbox;
  private final AdapterRegistryClient registryClient;
  private final WelcomeMessage welcomeMessage;

  ControlClient(PubSubSocketPair pubSubSocketPair, ReactiveInbox reactiveDataInbox, ReactiveInbox reactiveSubscriptionInbox,
      SessionManager sessionManager, AdapterRegistryClient registryClient, WelcomeMessage welcomeMessage) {
    this.pubSubSocketPair = pubSubSocketPair;
    this.reactiveDataInbox = reactiveDataInbox;
    this.reactiveSubscriptionInbox = reactiveSubscriptionInbox;
    this.sessionManager = sessionManager;
    this.registryClient = registryClient;
    this.welcomeMessage = welcomeMessage;
  }

  public static ControlClient create(Reactor reactor, BootstrapConfiguration configuration) throws ExecutionException, InterruptedException {
    String name = "adapter-" + UUID.randomUUID();

    PubSubSocketPair pubSubSocketPair = PubSubSocketPair.create(reactor);
    PubSubConnection pubSubConnection = pubSubSocketPair.connection();

    SessionManager sessionManager = SimpleSessionManager.create(name);
    AdapterRegistryClient registryClient = AdapterRegistryClient.create(pubSubConnection.publisher(), name);

    HighLevelInbox highLevelInbox = new HighLevelInbox(pubSubConnection.subscriptionManager());
    highLevelInbox.add(sessionManager);
    highLevelInbox.add(registryClient);

    ReactiveInbox reactiveDataInbox = ReactiveInbox.create(pubSubConnection.dataInbox(), highLevelInbox);

    pubSubSocketPair.subscriberSocketConnector().connect(configuration.getCoreHost(), configuration.getCorePort());
    WelcomeMessage welcomeMessage = sessionManager.getWelcomeMessage().get();//todo log state

    ReactiveInbox reactiveSubscriptionInbox = ReactiveInbox.create(pubSubConnection.subEventInbox(), new SubscriptionEventProcessor(sessionManager));
    pubSubSocketPair.publisherSocketConnector().connect(welcomeMessage.getHost(), welcomeMessage.getControlSubPort());
    sessionManager.awaitSessionReady();//todo log state

    return new ControlClient(pubSubSocketPair, reactiveDataInbox, reactiveSubscriptionInbox, sessionManager, registryClient, welcomeMessage);
  }

  public WelcomeMessage welcomeMessage() {
    return welcomeMessage;
  }

  public RegistryClient registryClient() {
    return registryClient;
  }

  @Override
  public void close() {
    pubSubSocketPair.close();
    reactiveDataInbox.close();
    reactiveSubscriptionInbox.close();
    sessionManager.close();
    registryClient.close();
  }

}
