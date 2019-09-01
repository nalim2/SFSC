package de.unistuttgart.isw.sfsc.dockercore;

import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class DockerConfiguration {

  private static final String DEFAULT_HAZELCAST_PORT = "5701";
  private static final String DEFAULT_BACKEND_PORT = "1251";
  private static final String DEFAULT_CONTROL_PUB_PORT = "1252";
  private static final String DEFAULT_CONTROL_SUB_PORT = "1253";
  private static final String DEFAULT_DATA_PUB_PORT = "1254";
  private static final String DEFAULT_DATA_SUB_PORT = "1255";

  private final String host;
  private final String backendHost;
  private final String hazelcastPort;
  private final String backendPort;
  private final String controlPubPort;
  private final String controlSubPort;
  private final String dataPubPort;
  private final String dataSubPort;

  DockerConfiguration(String host, String backendHost, String hazelcastPort, String backendPort, String controlPubPort, String controlSubPort,
      String dataPubPort, String dataSubPort) {
    this.host = Objects.requireNonNull(host);
    this.backendHost = backendHost;
    this.hazelcastPort = hazelcastPort;
    this.backendPort = backendPort;
    this.controlPubPort = controlPubPort;
    this.controlSubPort = controlSubPort;
    this.dataPubPort = dataPubPort;
    this.dataSubPort = dataSubPort;
  }

  static DockerConfiguration fromEnvironment() {
    Map<String, String> environment = System.getenv();
    String host = environment.get("HOST");
    String backendHost = environment.get("BACKEND_HOST");
    String hazelcastPort = environment.get("HAZELCAST_PORT");
    String backendPort = environment.get("BACKEND_PORT");
    String controlPubPort = environment.get("CONTROL_PUB_PORT");
    String controlSubPort = environment.get("CONTROL_SUB_PORT");
    String dataPubPort = environment.get("DATA_PUB_PORT");
    String dataSubPort = environment.get("DATA_SUB_PORT");
    return new DockerConfiguration(host, backendHost, hazelcastPort, backendPort, controlPubPort, controlSubPort, dataPubPort, dataSubPort);
  }

  Configuration<CoreOption> toCoreConfiguration() {
    EnumMap<CoreOption, String> enumMap = new EnumMap<>(CoreOption.class);
    enumMap.put(CoreOption.HOST, getHost());
    enumMap.put(CoreOption.BACKEND_HOST, getBackendHost());
    enumMap.put(CoreOption.HAZELCAST_PORT, getHazelcastPort());
    enumMap.put(CoreOption.BACKEND_PORT, getBackendPort());
    enumMap.put(CoreOption.CONTROL_PUB_PORT, getControlPubPort());
    enumMap.put(CoreOption.CONTROL_SUB_PORT, getControlSubPort());
    enumMap.put(CoreOption.DATA_PUB_PORT, getDataPubPort());
    enumMap.put(CoreOption.DATA_SUB_PORT, getDataSubPort());
    return Configuration.create(enumMap, CoreOption.class);
  }

  public String getHost() {
    return host;
  }

  public String getHazelcastPort() {
    return Optional.ofNullable(hazelcastPort).orElse(DEFAULT_HAZELCAST_PORT);
  }

  public String getBackendHost() {
    return Optional.ofNullable(backendHost).orElseGet(this::getHost);
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
}
