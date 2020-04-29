package de.unistuttgart.isw.sfsc.core.control.bootstrapping;

import com.google.protobuf.ByteString;

public class BootstrapperParameter {
  private final ByteString topic;
  private final int coreControlPubTcpPort;
  private final int coreControlSubTcpPort;
  private final int coreDataPubTcpPort;
  private final int coreDataSubTcpPort;
  private final String coreControlPubIpcFile;
  private final String coreControlSubIpcFile;
  private final String coreDataPubIpcFile;
  private final String coreDataSubIpcFile;

  public BootstrapperParameter(String topic, int coreControlPubTcpPort, int coreControlSubTcpPort, int coreDataPubTcpPort, int coreDataSubTcpPort,
      String coreControlPubIpcFile, String coreControlSubIpcFile, String coreDataPubIpcFile, String coreDataSubIpcFile) {
    this.topic = ByteString.copyFromUtf8(topic);
    this.coreControlPubTcpPort = coreControlPubTcpPort;
    this.coreControlSubTcpPort = coreControlSubTcpPort;
    this.coreDataPubTcpPort = coreDataPubTcpPort;
    this.coreDataSubTcpPort = coreDataSubTcpPort;
    this.coreControlPubIpcFile = coreControlPubIpcFile;
    this.coreControlSubIpcFile = coreControlSubIpcFile;
    this.coreDataPubIpcFile = coreDataPubIpcFile;
    this.coreDataSubIpcFile = coreDataSubIpcFile;
  }

  public ByteString getTopic() {
    return topic;
  }

  public int getCoreControlPubTcpPort() {
    return coreControlPubTcpPort;
  }

  public int getCoreControlSubTcpPort() {
    return coreControlSubTcpPort;
  }

  public int getCoreDataPubTcpPort() {
    return coreDataPubTcpPort;
  }

  public int getCoreDataSubTcpPort() {
    return coreDataSubTcpPort;
  }

  public String getCoreControlPubIpcFile() {
    return coreControlPubIpcFile;
  }

  public String getCoreControlSubIpcFile() {
    return coreControlSubIpcFile;
  }

  public String getCoreDataPubIpcFile() {
    return coreDataPubIpcFile;
  }

  public String getCoreDataSubIpcFile() {
    return coreDataSubIpcFile;
  }
}
