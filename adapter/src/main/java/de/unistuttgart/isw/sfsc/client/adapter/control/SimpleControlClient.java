package de.unistuttgart.isw.sfsc.client.adapter.control;

import de.unistuttgart.isw.sfsc.client.adapter.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.client.adapter.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.client.adapter.registry.SimpleRegistryClient;
import de.unistuttgart.isw.sfsc.client.adapter.session.SessionManager;
import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import zmq.processors.MessageDistributor;
import zmq.pubsubsocketpair.SimplePubSubSocketPair;
import zmq.reactiveinbox.ReactiveInbox;
import zmq.reactor.Reactor;

public class SimpleControlClient implements ControlClient, AutoCloseable {

  private final SimplePubSubSocketPair pubSubSocketPair;
  private final SessionManager sessionManager;
  private final ReactiveInbox reactiveDataInbox;
  private final ReactiveInbox reactiveSubscriptionInbox;
  private final SimpleRegistryClient registryClient;
  private final WelcomeMessage welcomeMessage;

  SimpleControlClient(SimplePubSubSocketPair pubSubSocketPair, ReactiveInbox reactiveDataInbox, ReactiveInbox reactiveSubscriptionInbox,
      SessionManager sessionManager, SimpleRegistryClient registryClient, WelcomeMessage welcomeMessage) {
    this.pubSubSocketPair = pubSubSocketPair;
    this.reactiveDataInbox = reactiveDataInbox;
    this.reactiveSubscriptionInbox = reactiveSubscriptionInbox;
    this.sessionManager = sessionManager;
    this.registryClient = registryClient;
    this.welcomeMessage = welcomeMessage;
  }

  public static SimpleControlClient create(Reactor reactor, BootstrapConfiguration configuration) throws ExecutionException, InterruptedException {
    UUID uuid = UUID.randomUUID();
    SimplePubSubSocketPair pubSubSocketPair = SimplePubSubSocketPair.create(reactor);

    WelcomeMessageTransmitter welcomeMessageTransmitter = new WelcomeMessageTransmitter();
    SessionManager sessionManager = SessionManager.create(welcomeMessageTransmitter, uuid);
    SimpleRegistryClient registryClient = SimpleRegistryClient.create(pubSubSocketPair.publisher(), uuid);
    MessageDistributor messageDistributor = new MessageDistributor();
    messageDistributor.add(sessionManager);
    messageDistributor.add(registryClient);

    ReactiveInbox reactiveDataInbox = ReactiveInbox.create(pubSubSocketPair.dataInbox(), messageDistributor);
    pubSubSocketPair.subscriberSocketConnector().connect(configuration.getCoreHost(), configuration.getCorePort());
    pubSubSocketPair.subscriptionManager().subscribe(sessionManager.getTopic().getBytes());
    pubSubSocketPair.subscriptionManager().subscribe(registryClient.getTopic().getBytes());

    WelcomeMessage welcomeMessage = welcomeMessageTransmitter.welcomeMessageFuture().get();//todo log state

    FutureTask<Void> ready = new FutureTask<>(() -> null);
    ReactiveInbox reactiveSubscriptionInbox = ReactiveInbox.create(pubSubSocketPair.subEventInbox(), new SubscriptionEventInboxHandler(ready));
    pubSubSocketPair.publisherSocketConnector().connect(welcomeMessage.getHost(), welcomeMessage.getControlSubPort());
    ready.get();//todo log state
    return new SimpleControlClient(pubSubSocketPair, reactiveDataInbox, reactiveSubscriptionInbox, sessionManager, registryClient, welcomeMessage);
  }

  @Override
  public WelcomeMessage welcomeMessage() {
    return welcomeMessage;
  }

  @Override
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

  static class WelcomeMessageTransmitter implements Consumer<WelcomeMessage> {

    private volatile WelcomeMessage welcomeMessage;
    private final FutureTask<WelcomeMessage> welcomeMessageFuture = new FutureTask<>(() -> welcomeMessage);

    @Override
    public void accept(WelcomeMessage welcomeMessage) {
      this.welcomeMessage = welcomeMessage;
      welcomeMessageFuture.run();
    }

    Future<WelcomeMessage> welcomeMessageFuture() {
      return welcomeMessageFuture;
    }
  }

}
