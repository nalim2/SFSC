package de.unistuttgart.isw.sfsc.core.control;

import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import zmq.pubsubsocketpair.PubSubSocketPair;
import zmq.pubsubsocketpair.SimplePubSubSocketPair;
import zmq.reactor.ContextConfiguration;
import zmq.reactor.Reactor;

public class Control implements AutoCloseable {

  private final ExecutorService executorService = Executors.newCachedThreadPool();
  private final SimplePubSubSocketPair pubSubSocketPair;
  private final ControlInboxHandler controlMessageHandler;
  private final SubscriptionEventInboxHandler subscriptionEventInboxHandler;
  private final Reactor reactor;

  Control(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration) throws ExecutionException, InterruptedException {
    reactor = Reactor.create(contextConfiguration);
    pubSubSocketPair = SimplePubSubSocketPair.create(reactor);
    controlMessageHandler = ControlInboxHandler.create(pubSubSocketPair, executorService, configuration);
    subscriptionEventInboxHandler = SubscriptionEventInboxHandler.create(pubSubSocketPair, configuration);
  }

  public static Control create(ContextConfiguration contextConfiguration, Configuration<CoreOption> configuration)
      throws ExecutionException, InterruptedException {
    Control control = new Control(contextConfiguration, configuration);
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
    subscriptionEventInboxHandler.close();
    controlMessageHandler.close();
    executorService.shutdownNow();
  }
}
