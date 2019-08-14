package de.unistuttgart.isw.sfsc.core;

import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import de.unistuttgart.isw.sfsc.core.control.Control;
import de.unistuttgart.isw.sfsc.core.pubsub.PubSub;
import de.unistuttgart.isw.sfsc.core.serf.SerfEndpoint;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import zmq.reactor.ContextConfiguration;
import zmq.reactor.Reactor;

public class Core implements AutoCloseable {

  private final Reactor reactor;
  private final Control control;
  private final PubSub pubSub;
  private final SerfEndpoint serfEndpoint;

  Core(Reactor reactor, Configuration<CoreOption> configuration) throws IOException, ExecutionException, InterruptedException {
    this.reactor = reactor;
    this.control = Control.create(reactor, configuration);
    this.pubSub = PubSub.create(reactor, configuration);
    this.serfEndpoint = SerfEndpoint.getInstance(configuration);
  }

  public static Core start(Configuration<CoreOption> configuration) throws IOException, ExecutionException, InterruptedException {
    ContextConfiguration contextConfiguration = context -> {
      context.setLinger(0);
      context.setRcvHWM(0);
      context.setSndHWM(0);
    };
    Reactor reactor = Reactor.create(contextConfiguration);
    Core core = new Core(reactor, configuration);
    core.serfEndpoint.handleMembershipEventStream(core.serfEndpoint.memberJoinStream(), core.pubSub::connectBackend);
    return core;
  }

  @Override
  public void close() throws Exception {
    reactor.close();
    serfEndpoint.close();
    control.close();
    pubSub.close();
  }
}
