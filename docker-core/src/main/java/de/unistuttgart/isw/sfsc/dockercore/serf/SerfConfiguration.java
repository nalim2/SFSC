package de.unistuttgart.isw.sfsc.dockercore.serf;

import de.unistuttgart.isw.sfsc.dockercore.DockerConfiguration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SerfConfiguration {

  private final Map<SerfOption, String> options;

  SerfConfiguration(EnumMap<SerfOption, String> options) {
    this.options = new EnumMap<>(options);
  }

  public static SerfConfiguration fromConfiguration(DockerConfiguration dockerConfiguration) {
    EnumMap<SerfOption, String> options = new EnumMap<>(SerfOption.class);
    options.put(SerfOption.TAG_PORT, dockerConfiguration.getBackendPort());
    options.put(SerfOption.BIND_ADDRESS, dockerConfiguration.getBackendHost() + ":" + dockerConfiguration.getSerfPort());
    dockerConfiguration.getNodeId().ifPresent(nodeId -> options.put(SerfOption.NODE, nodeId));
    dockerConfiguration.getClusterName().ifPresent(clusterName -> options.put(SerfOption.DISCOVER, clusterName));
    dockerConfiguration.getSerfJoinAddress().ifPresent(joinAddress -> {
      options.put(SerfOption.JOIN_ADDRESS, joinAddress);
      delay();
    });
    return new SerfConfiguration(options);
  }

  public List<String> toCommand() {
    List<String> serfCommand = new ArrayList<>();
    serfCommand.add("serf");
    serfCommand.add("agent");
    options.forEach((option, value) -> serfCommand.add(option.getCommandString(value)));
    return serfCommand;
  }

  static void delay() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
