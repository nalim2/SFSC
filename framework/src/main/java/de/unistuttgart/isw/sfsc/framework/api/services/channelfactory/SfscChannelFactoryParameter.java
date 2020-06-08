package de.unistuttgart.isw.sfsc.framework.api.services.channelfactory;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.framework.api.services.clientserver.SfscServerParameter;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import java.util.Map;

public class SfscChannelFactoryParameter {

  private String serviceName;
  private Map<String, ByteString> customTags;
  private ByteString inputTopic;
  private ByteString inputMessageType;
  private Integer timeoutMs;
  private Integer sendRateMs;
  private Integer sendMaxTries;

  public SfscChannelFactoryParameter setServiceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  public SfscChannelFactoryParameter setCustomTags(Map<String, ByteString> customTags) {
    this.customTags = customTags;
    return this;
  }

  public SfscChannelFactoryParameter setInputTopic(ByteString inputTopic) {
    this.inputTopic = inputTopic;
    return this;
  }

  public SfscChannelFactoryParameter setInputMessageType(ByteString inputMessageType) {
    this.inputMessageType = inputMessageType;
    return this;
  }

  public SfscChannelFactoryParameter setTimeoutMs(Integer timeoutMs) {
    if (sendMaxTries != null && sendMaxTries < 0) {
      throw new IllegalArgumentException();
    }
    this.timeoutMs = timeoutMs;
    return this;
  }

  public SfscChannelFactoryParameter setSendRateMs(Integer sendRateMs) {
    if (sendMaxTries != null && sendMaxTries < 0) {
      throw new IllegalArgumentException();
    }
    this.sendRateMs = sendRateMs;
    return this;
  }

  public SfscChannelFactoryParameter setSendMaxTries(Integer sendMaxTries) {
    if (sendMaxTries != null && sendMaxTries < 0) {
      throw new IllegalArgumentException();
    }
    this.sendMaxTries = sendMaxTries;
    return this;
  }

  String getServiceName() {
    return serviceName;
  }

  Map<String, ByteString> getCustomTags() {
    return customTags;
  }

  ByteString getInputTopic() {
    return inputTopic;
  }

  ByteString getInputMessageType() {
    return inputMessageType;
  }

  public Integer getTimeoutMs() {
    return timeoutMs;
  }

  public Integer getSendRateMs() {
    return sendRateMs;
  }

  public Integer getSendMaxTries() {
    return sendMaxTries;
  }

  public SfscServerParameter toSfscServerParameter() {
    return new SfscServerParameter()
        .setServiceName(serviceName)
        .setCustomTags(customTags)
        .setInputTopic(inputTopic)
        .setInputMessageType(inputMessageType)
        .setOutputMessageType(ByteString.copyFromUtf8(SfscServiceDescriptor.class.getCanonicalName()))
        .setTimeoutMs(timeoutMs)
        .setSendMaxTries(sendMaxTries);
  }
}
