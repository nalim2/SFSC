package de.unistuttgart.isw.sfsc.core.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.ReplicatedMap;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import de.unistuttgart.isw.sfsc.serverserver.registry.RegistryEntry;
import java.util.function.BiConsumer;

public class HazelcastNode implements NotThrowingAutoCloseable {

  private static final String BACKEND_PORT_ATTRIBUTE_KEY = "backendPort";
  private static final String REGISTRY_MAP_NAME = "ServiceRegistry";

  private final HazelcastInstance hazelcastInstance;
  private final ReplicatedMap<RegistryEntry, Boolean> replicatedMap; //for some reason, ReplicatedMap does a nn check on value. Void -> Boolean

  HazelcastNode(HazelcastInstance hazelcastInstance) {
    this.hazelcastInstance = hazelcastInstance;
    replicatedMap = hazelcastInstance.getReplicatedMap(REGISTRY_MAP_NAME);
  }

  public static HazelcastNode create(BiConsumer<String, Integer> memberAddedEventConsumer, BiConsumer<String, Integer> memberRemovedEventConsumer,
      Configuration<CoreOption> configuration, String coreId) {
    Config config = new Config();
    config.addListenerConfig(new ListenerConfig(new BackendEventConsumer(memberAddedEventConsumer, memberRemovedEventConsumer)));
    config.getMemberAttributeConfig().setIntAttribute(BACKEND_PORT_ATTRIBUTE_KEY, Integer.parseInt(configuration.get(CoreOption.BACKEND_PORT)));
    config.getNetworkConfig()
        .setPublicAddress(configuration.get(CoreOption.BACKEND_HOST))
        .setPort(Integer.parseInt(configuration.get(CoreOption.HAZELCAST_PORT)));
    HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);

    return new HazelcastNode(hazelcastInstance);
  }

  public ReplicatedMap<RegistryEntry, Boolean> getReplicatedMap() {
    return replicatedMap;
  }

  @Override
  public void close() {
    hazelcastInstance.shutdown();
  }

  static String getHost(MembershipEvent membershipEvent) {
    return membershipEvent.getMember().getAddress().getHost();
  }

  static int getPort(MembershipEvent membershipEvent) {
    return membershipEvent.getMember().getIntAttribute(BACKEND_PORT_ATTRIBUTE_KEY);
  }
}
