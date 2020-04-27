package de.unistuttgart.isw.sfsc.core.configuration;

import java.util.Optional;

public class EnvironmentReader {

  private static final String coreIdEnvVar = System.getenv("CORE_ID");
  private static final String hostEnvVar = System.getenv("HOST");
  private static final String backendHostEnvVar = System.getenv("BACKEND_HOST");
  private static final String controlPubPortEnvVar = System.getenv("CONTROL_PUB_PORT");
  private static final String controlSubPortEnvVar = System.getenv("CONTROL_SUB_PORT");
  private static final String controlBackendPortEnvVar = System.getenv("CONTROL_BACKEND_PORT");
  private static final String dataPubPortEnvVar = System.getenv("DATA_PUB_PORT");
  private static final String dataSubPortEnvVar = System.getenv("DATA_SUB_PORT");
  private static final String dataBackendPortEnvVar = System.getenv("DATA_BACKEND_PORT");
  private static final String heartbeatSendRateMsEnvVar = System.getenv("HEARTBEAT_SEND_RATE_MS");
  private static final String heartbeatDeadlineIncomingMs = System.getenv("HEARTBEAT_DEADLINE_INCOMING_MS");

  public Optional<String> getCoreId() {
    return Optional.ofNullable(coreIdEnvVar);
  }

  public Optional<String> getHost() {
    return Optional.ofNullable(hostEnvVar);
  }

  public Optional<String> getBackendHost() {
    return Optional.ofNullable(backendHostEnvVar);
  }

  public Optional<Integer> getControlPubPort() {
    return Optional.ofNullable(controlPubPortEnvVar).map(Integer::parseInt);
  }

  public Optional<Integer> getControlSubPort() {
    return Optional.ofNullable(controlSubPortEnvVar).map(Integer::parseInt);
  }

  public Optional<Integer> getControlBackendPort() {
    return Optional.ofNullable(controlBackendPortEnvVar).map(Integer::parseInt);
  }

  public Optional<Integer> getDataPubPort() {
    return Optional.ofNullable(dataPubPortEnvVar).map(Integer::parseInt);
  }

  public Optional<Integer> getDataSubPort() {
    return Optional.ofNullable(dataSubPortEnvVar).map(Integer::parseInt);
  }

  public Optional<Integer> getDataBackendPort() {
    return Optional.ofNullable(dataBackendPortEnvVar).map(Integer::parseInt);
  }

  public Optional<Integer> getHeartbeatSendRateMs() {
    return Optional.ofNullable(heartbeatSendRateMsEnvVar).map(Integer::parseInt);
  }

  public Optional<Integer> getHeartbeatDeadlineIncomingMs() {
    return Optional.ofNullable(heartbeatDeadlineIncomingMs).map(Integer::parseInt);
  }
}
