package de.unistuttgart.isw.sfsc.core.hazelcast;

import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.replicatedmap.ReplicatedMap;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.core.CoreParameter;
import de.unistuttgart.isw.sfsc.serverserver.registry.RegistryEntry;
import java.util.function.BiConsumer;

public class HazelcastNode implements NotThrowingAutoCloseable {

  private static final String BACKEND_PORT_ATTRIBUTE_KEY = "dataBackendPort";
  private static final String REGISTRY_MAP_NAME = "ServiceRegistry";

  private final HazelcastInstance hazelcastInstance;
  private final ReplicatedMap<RegistryEntry, Boolean> replicatedMap; //for some reason, ReplicatedMap does a nn check on value. Void -> Boolean

  HazelcastNode(HazelcastInstance hazelcastInstance) {
    this.hazelcastInstance = hazelcastInstance;
    replicatedMap = hazelcastInstance.getReplicatedMap(REGISTRY_MAP_NAME);
  }

  public static HazelcastNode create(BiConsumer<String, Integer> memberAddedEventConsumer, BiConsumer<String, Integer> memberRemovedEventConsumer,
      CoreParameter parameter) {
    Config config = new Config();
    config.addListenerConfig(new ListenerConfig(new BackendEventConsumer(memberAddedEventConsumer, memberRemovedEventConsumer)));
    config.getMemberAttributeConfig().setAttribute(BACKEND_PORT_ATTRIBUTE_KEY, Integer.toString(parameter.getDataBackendPort()));
    config.getNetworkConfig()
        .setPublicAddress(parameter.getBackendHost())
        .setPort(parameter.getControlBackendPort());
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
    return Integer.parseInt(membershipEvent.getMember().getAttribute(BACKEND_PORT_ATTRIBUTE_KEY));
  }
}
