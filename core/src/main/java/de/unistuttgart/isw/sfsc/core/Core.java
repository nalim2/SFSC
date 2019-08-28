package de.unistuttgart.isw.sfsc.core;

import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import de.unistuttgart.isw.sfsc.core.control.Control;
import de.unistuttgart.isw.sfsc.core.data.Data;
import de.unistuttgart.isw.sfsc.core.hazelcast.HazelcastNode;
import java.util.concurrent.ExecutionException;
import zmq.reactor.ContextConfiguration;

public class Core implements AutoCloseable {

  private final Control control;
  private final Data data;
  private final HazelcastNode hazelcastNode;

  Core(Control control, Data data, HazelcastNode hazelcastNode) {
    this.control = control;
    this.data = data;
    this.hazelcastNode = hazelcastNode;
  }

  public static Core start(Configuration<CoreOption> configuration) throws ExecutionException, InterruptedException {
    ContextConfiguration contextConfiguration = context -> {
      context.setRcvHWM(0);
      context.setSndHWM(0);
    };
    Data data = Data.create(contextConfiguration, configuration);
    HazelcastNode hazelcastNode = HazelcastNode.create(data::connectBackend, data::disconnectBackend, configuration);
    Control control = Control.create(contextConfiguration, configuration, hazelcastNode.getRegistry());

    return new Core(control, data, hazelcastNode);
  }

  @Override
  public void close() {
    hazelcastNode.close();
    control.close();
    data.close();
  }
}
