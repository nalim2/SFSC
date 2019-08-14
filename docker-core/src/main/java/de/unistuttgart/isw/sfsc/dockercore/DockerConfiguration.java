package de.unistuttgart.isw.sfsc.dockercore;

import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DockerConfiguration {

  private static final String DEFAULT_SERF_PORT = "7946";
  private static final String DEFAULT_SERF_RPC_HOST = "127.0.0.1";
  private static final String DEFAULT_SERF_RPC_PORT = "7373";
  private static final String DEFAULT_BACKEND_PORT = "1251";
  private static final String DEFAULT_CONTROL_PUB_PORT = "1252";
  private static final String DEFAULT_CONTROL_SUB_PORT = "1253";
  private static final String DEFAULT_DATA_PUB_PORT = "1254";
  private static final String DEFAULT_DATA_SUB_PORT = "1255";
  private static final String DEFAULT_REGISTRY_PUB_PORT = "1256";
  private static final String DEFAULT_REGISTRY_SUB_PORT = "1257";

  private final String nodeId;
  private final String clusterName;
  private final String host;
  private final String serfJoinAddress;
  private final String backendHost;
  private final String serfPort;
  private final String backendPort;
  private final String controlPubPort;
  private final String controlSubPort;
  private final String dataPubPort;
  private final String dataSubPort;
  private final String registryPubPort;
  private final String registrySubPort;

  DockerConfiguration(String nodeId, String clusterName, String host, String serfJoinAddress, String backendHost, String serfPort, String backendPort,
      String controlPubPort, String controlSubPort, String dataPubPort, String dataSubPort, String registryPubPort, String registrySubPort) {
    this.host = Objects.requireNonNull(host);
    this.clusterName = clusterName;
    this.nodeId = nodeId;
    this.serfJoinAddress = serfJoinAddress;
    this.backendHost = backendHost;
    this.serfPort = serfPort;
    this.backendPort = backendPort;
    this.controlPubPort = controlPubPort;
    this.controlSubPort = controlSubPort;
    this.dataPubPort = dataPubPort;
    this.dataSubPort = dataSubPort;
    this.registryPubPort = registryPubPort;
    this.registrySubPort = registrySubPort;
  }

  static DockerConfiguration fromEnvironment() {
    Map<String, String> environment = System.getenv();
    String nodeId = environment.get("NODE_ID");
    String clusterName = environment.get("CLUSTER_NAME");
    String host = environment.get("HOST");
    String serfJoinAddress = environment.get("SERF_JOIN_ADDRESS");
    String backendHost = environment.get("BACKEND_HOST");
    String serfPort = environment.get("SERF_PORT");
    String backendPort = environment.get("BACKEND_PORT");
    String controlPubPort = environment.get("CONTROL_PUB_PORT");
    String controlSubPort = environment.get("CONTROL_SUB_PORT");
    String dataPubPort = environment.get("DATA_PUB_PORT");
    String dataSubPort = environment.get("DATA_SUB_PORT");
    String registryPubPort = environment.get("REGISTRY_PUB_PORT");
    String registrySubPort = environment.get("REGISTRY_SUB_PORT");
    return new DockerConfiguration(nodeId, clusterName, host, serfJoinAddress, backendHost, serfPort, backendPort, controlPubPort, controlSubPort,
        dataPubPort, dataSubPort, registryPubPort, registrySubPort);
  }

  Configuration<CoreOption> toCoreConfiguration() {
    EnumMap<CoreOption, String> enumMap = new EnumMap<>(CoreOption.class);
    enumMap.put(CoreOption.HOST, getHost());
    enumMap.put(CoreOption.SERF_RPC_HOST, DEFAULT_SERF_RPC_HOST);
    enumMap.put(CoreOption.SERF_RPC_PORT, DEFAULT_SERF_RPC_PORT);
    enumMap.put(CoreOption.BACKEND_PORT, getBackendPort());
    enumMap.put(CoreOption.CONTROL_PUB_PORT, getControlPubPort());
    enumMap.put(CoreOption.CONTROL_SUB_PORT, getControlSubPort());
    enumMap.put(CoreOption.DATA_PUB_PORT, getDataPubPort());
    enumMap.put(CoreOption.DATA_SUB_PORT, getDataSubPort());
    enumMap.put(CoreOption.REGISTRY_PUB_PORT, getRegistryPubPort());
    enumMap.put(CoreOption.REGISTRY_SUB_PORT, getRegistrySubPort());
    return Configuration.create(enumMap, CoreOption.class);
  }

  public String getHost() {
    return host;
  }

  public String getBackendHost() {
    return Optional.ofNullable(backendHost).orElseGet(this::getHost);
  }

  public String getSerfPort() {
    return Optional.ofNullable(serfPort).orElse(DEFAULT_SERF_PORT);
  }

  public Optional<String> getNodeId() {
    return Optional.ofNullable(nodeId);
  }

  public Optional<String> getClusterName() {
    return Optional.ofNullable(clusterName);
  }

  public Optional<String> getSerfJoinAddress() {
    return Optional.ofNullable(serfJoinAddress);
  }

  public String getBackendPort() {
    return Optional.ofNullable(backendPort).orElse(DEFAULT_BACKEND_PORT);
  }

  public String getControlPubPort() {
    return Optional.ofNullable(controlPubPort).orElse(DEFAULT_CONTROL_PUB_PORT);
  }

  public String getControlSubPort() {
    return Optional.ofNullable(controlSubPort).orElse(DEFAULT_CONTROL_SUB_PORT);
  }

  public String getDataPubPort() {
    return Optional.ofNullable(dataPubPort).orElse(DEFAULT_DATA_PUB_PORT);
  }

  public String getDataSubPort() {
    return Optional.ofNullable(dataSubPort).orElse(DEFAULT_DATA_SUB_PORT);
  }

  public String getRegistryPubPort() {
    return Optional.ofNullable(registryPubPort).orElse(DEFAULT_REGISTRY_PUB_PORT);
  }

  public String getRegistrySubPort() {
    return Optional.ofNullable(registrySubPort).orElse(DEFAULT_REGISTRY_SUB_PORT);
  }
}
