package de.unistuttgart.isw.sfsc.core.control;

import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import protocol.control.ControlProtocol;
import zmq.pubsubsocketpair.PubSubSocketPair;
import zmq.reactor.Reactor;

public class Control implements AutoCloseable {

  private final ExecutorService executorService = Executors.newCachedThreadPool();
  private final PubSubSocketPair pubSubSocketPair;
  private final ControlInboxHandler controlMessageHandler;
  private final SubscriptionEventInboxHandler subscriptionEventInboxHandler;

  Control(Reactor reactor, Configuration<CoreOption> configuration) throws ExecutionException, InterruptedException {
    pubSubSocketPair = PubSubSocketPair.create(reactor, ControlProtocol.class);
    controlMessageHandler = ControlInboxHandler.create(pubSubSocketPair, executorService, configuration);
    subscriptionEventInboxHandler = SubscriptionEventInboxHandler.create(pubSubSocketPair, configuration);
  }

  public static Control create(Reactor reactor, Configuration<CoreOption> configuration) throws ExecutionException, InterruptedException {
    Control control = new Control(reactor, configuration);
    bind(control.pubSubSocketPair, configuration);
    return control;
  }

  static void bind(PubSubSocketPair pubSubSocketPair, Configuration<CoreOption> configuration) {
    pubSubSocketPair.getPublisherSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.CONTROL_PUB_PORT)));
    pubSubSocketPair.getSubscriberSocketConnector().bind(Integer.parseInt(configuration.get(CoreOption.CONTROL_SUB_PORT)));
  }

  @Override
  public void close() throws Exception {
    pubSubSocketPair.close();
    subscriptionEventInboxHandler.close();
    controlMessageHandler.close();
    executorService.shutdownNow();
  }
}
