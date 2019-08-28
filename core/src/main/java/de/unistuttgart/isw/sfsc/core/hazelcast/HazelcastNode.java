package de.unistuttgart.isw.sfsc.core.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MembershipEvent;
import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class HazelcastNode implements AutoCloseable {

  private static final String BACKEND_PORT_ATTRIBUTE_KEY = "backendPort";
  private static final String REGISTRY_MAP_NAME = "ServiceRegistry";

  private final HazelcastInstance hazelcastInstance;
  private final Registry registry; //todo remove All service from member on memberquit

  static final Function<MembershipEvent, String> GET_HOST = event -> event.getMember().getAddress().getHost();
  static final Function<MembershipEvent, Integer> GET_PORT = event -> event.getMember().getIntAttribute(BACKEND_PORT_ATTRIBUTE_KEY);

  HazelcastNode(HazelcastInstance hazelcastInstance) {
    this.hazelcastInstance = hazelcastInstance;
    registry = new RegistryImpl(hazelcastInstance.getReplicatedMap(REGISTRY_MAP_NAME));
  }

  public static HazelcastNode create(BiConsumer<String, Integer> memberAddedEventConsumer, BiConsumer<String, Integer> memberRemovedEventConsumer,
      Configuration<CoreOption> configuration) {
    Config config = new Config();
    config.addListenerConfig(new ListenerConfig(new BackendEventConsumer(memberAddedEventConsumer, memberRemovedEventConsumer)));
    config.getMemberAttributeConfig()
        .setIntAttribute(BACKEND_PORT_ATTRIBUTE_KEY, Integer.parseInt(configuration.get(CoreOption.BACKEND_PORT)));
    config.getNetworkConfig()
        .setPublicAddress(configuration.get(CoreOption.BACKEND_HOST))
        .setPort(Integer.parseInt(configuration.get(CoreOption.HAZELCAST_PORT)));
    HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
    return new HazelcastNode(hazelcastInstance);
  }

  public Registry getRegistry(){
    return registry;
  }

  @Override
  public void close() {
    hazelcastInstance.shutdown();
  }
}
