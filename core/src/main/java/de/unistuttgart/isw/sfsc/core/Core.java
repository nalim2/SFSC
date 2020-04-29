package de.unistuttgart.isw.sfsc.core;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.core.configuration.CoreConfiguration;
import de.unistuttgart.isw.sfsc.core.control.Control;
import de.unistuttgart.isw.sfsc.core.data.Data;
import de.unistuttgart.isw.sfsc.core.hazelcast.HazelcastNode;
import de.unistuttgart.isw.sfsc.core.hazelcast.registry.Registry;
import java.io.File;
import java.io.IOException;
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

  public static Core start() throws ExecutionException, InterruptedException, IOException {
    return start(new CoreConfiguration());
  }

  public static Core start(CoreConfiguration configuration) throws ExecutionException, InterruptedException, IOException {
    return start(configuration.createCoreParameter());
  }

  static Core start(CoreParameter parameter) throws ExecutionException, InterruptedException, IOException {
    new File(parameter.getIpcFolderLocation()).mkdirs();
    Data data = Data.create(parameter);
    HazelcastNode hazelcastNode = HazelcastNode.create(data::connectBackend, data::disconnectBackend, parameter);
    Registry registry = new Registry(hazelcastNode.getReplicatedMap(), parameter.getCoreId());
    Control control = Control.create(parameter, registry);

    return new Core(control, data, hazelcastNode);
  }

  @Override
  public void close() {
    hazelcastNode.close();
    control.close();
    data.close();
  }
}
