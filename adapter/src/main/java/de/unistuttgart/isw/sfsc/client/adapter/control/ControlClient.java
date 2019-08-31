package de.unistuttgart.isw.sfsc.client.adapter.control;

import de.unistuttgart.isw.sfsc.client.adapter.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.client.adapter.control.registry.AdapterRegistryClient;
import de.unistuttgart.isw.sfsc.client.adapter.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.client.adapter.control.session.SessionManager;
import de.unistuttgart.isw.sfsc.client.adapter.control.session.SimpleSessionManager;
import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import zmq.processors.MessageDistributor;
import zmq.processors.SubscriptionEventProcessor;
import zmq.pubsubsocketpair.SimplePubSubSocketPair;
import zmq.reactiveinbox.ReactiveInbox;
import zmq.reactor.Reactor;

public class ControlClient implements AutoCloseable {

  private final SimplePubSubSocketPair pubSubSocketPair;
  private final SessionManager sessionManager;
  private final ReactiveInbox reactiveDataInbox;
  private final ReactiveInbox reactiveSubscriptionInbox;
  private final AdapterRegistryClient registryClient;
  private final WelcomeMessage welcomeMessage;

  ControlClient(SimplePubSubSocketPair pubSubSocketPair, ReactiveInbox reactiveDataInbox, ReactiveInbox reactiveSubscriptionInbox,
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

    SimplePubSubSocketPair pubSubSocketPair = SimplePubSubSocketPair.create(reactor);

    SessionManager sessionManager = SimpleSessionManager.create(name);
    AdapterRegistryClient registryClient = AdapterRegistryClient.create(pubSubSocketPair.publisher(), name);
    MessageDistributor messageDistributor = new MessageDistributor();
    messageDistributor.add(sessionManager);
    messageDistributor.add(registryClient);

    ReactiveInbox reactiveDataInbox = ReactiveInbox.create(pubSubSocketPair.dataInbox(), messageDistributor);
    pubSubSocketPair.subscriberSocketConnector().connect(configuration.getCoreHost(), configuration.getCorePort());
    pubSubSocketPair.subscriptionManager().subscribe(SessionManager.TOPIC);
    pubSubSocketPair.subscriptionManager().subscribe(AdapterRegistryClient.TOPIC);

    WelcomeMessage welcomeMessage = sessionManager.getWelcomeMessage().get();//todo log state

    ReactiveInbox reactiveSubscriptionInbox = ReactiveInbox.create(pubSubSocketPair.subEventInbox(), new SubscriptionEventProcessor(sessionManager));
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
