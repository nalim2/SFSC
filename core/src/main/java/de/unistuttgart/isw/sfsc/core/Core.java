package de.unistuttgart.isw.sfsc.core;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ContextConfiguration;
import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import de.unistuttgart.isw.sfsc.core.control.Control;
import de.unistuttgart.isw.sfsc.core.data.Data;
import de.unistuttgart.isw.sfsc.core.hazelcast.HazelcastNode;
import de.unistuttgart.isw.sfsc.core.hazelcast.registry.Registry;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Core implements NotThrowingAutoCloseable {

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
    String coreId = UUID.randomUUID().toString();

    Data data = Data.create(contextConfiguration, configuration);
    HazelcastNode hazelcastNode = HazelcastNode.create(data::connectBackend, data::disconnectBackend, configuration, coreId);
    Registry registry = new Registry(hazelcastNode.getReplicatedMap(), coreId);
    Control control = Control.create(contextConfiguration, configuration, registry);

    return new Core(control, data, hazelcastNode);
  }

  @Override
  public void close() {
    hazelcastNode.close();
    control.close();
    data.close();
  }
}
