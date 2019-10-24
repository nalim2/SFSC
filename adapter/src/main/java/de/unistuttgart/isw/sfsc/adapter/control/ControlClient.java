package de.unistuttgart.isw.sfsc.adapter.control;

import de.unistuttgart.isw.sfsc.adapter.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.adapter.control.bootstrapping.Bootstrapper;
import de.unistuttgart.isw.sfsc.adapter.control.registry.RegistryApi;
import de.unistuttgart.isw.sfsc.adapter.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.adapter.control.session.SessionManager;
import de.unistuttgart.isw.sfsc.adapter.control.session.WelcomeInformation;
import de.unistuttgart.isw.sfsc.clientserver.protocol.bootstrap.BootstrapMessage;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnectionImplementation;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubSocketPair;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ControlClient implements NotThrowingAutoCloseable {

  private final PubSubSocketPair pubSubSocketPair;
  private final PubSubConnectionImplementation pubSubConnection;
  private final SessionManager sessionManager;
  private final RegistryClient registryClient;

  ControlClient(PubSubSocketPair pubSubSocketPair, PubSubConnectionImplementation pubSubConnection, SessionManager sessionManager,
      RegistryClient registryClient) {
    this.pubSubSocketPair = pubSubSocketPair;
    this.pubSubConnection = pubSubConnection;
    this.sessionManager = sessionManager;
    this.registryClient = registryClient;
  }

  public static ControlClient create(Reactor reactor, BootstrapConfiguration configuration)
      throws ExecutionException, InterruptedException, TimeoutException {
    PubSubSocketPair pubSubSocketPair = PubSubSocketPair.create(reactor);
    PubSubConnectionImplementation pubSubConnection = PubSubConnectionImplementation.create(pubSubSocketPair);
    pubSubConnection.start();
    pubSubSocketPair.subscriberSocketConnector().connect(configuration.getCoreHost(), configuration.getCorePort());
    BootstrapMessage bootstrapMessage = Bootstrapper.getBootstrapMessage(pubSubConnection);
    pubSubSocketPair.publisherSocketConnector().connect(configuration.getCoreHost(), bootstrapMessage.getCoreSubscriptionPort());

    SessionManager sessionManager = SessionManager.create(pubSubConnection);

    RegistryClient registryClient = RegistryClient.create(pubSubConnection, sessionManager.getWelcomeInformation());

    return new ControlClient(pubSubSocketPair, pubSubConnection, sessionManager, registryClient);
  }

  public WelcomeInformation welcomeInformation() {
    return sessionManager.getWelcomeInformation();
  }

  public RegistryApi registryClient() {
    return registryClient;
  }

  @Override
  public void close() {
    pubSubSocketPair.close();
    pubSubConnection.close();
    sessionManager.close();
    registryClient.close();
  }

}
