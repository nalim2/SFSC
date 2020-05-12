package de.unistuttgart.isw.sfsc.framework.api;

import com.google.protobuf.ByteString;
import java.util.Map;

public class SfscPublisherParameter {

  private String serviceName;
  private Map<String, ByteString> customTags;
  private ByteString outputTopic;
  private ByteString outputMessageType;
  private Boolean unregistered;

  public SfscPublisherParameter setServiceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  public SfscPublisherParameter setCustomTags(Map<String, ByteString> customTags) {
    this.customTags = customTags;
    return this;
  }

  public SfscPublisherParameter setOutputTopic(ByteString outputTopic) {
    this.outputTopic = outputTopic;
    return this;
  }

  public SfscPublisherParameter setOutputMessageType(ByteString outputMessageType) {
    this.outputMessageType = outputMessageType;
    return this;
  }

  public SfscPublisherParameter setUnregistered(Boolean unregistered) {
    this.unregistered = unregistered;
    return this;
  }

  String getServiceName() {
    return serviceName;
  }

  Map<String, ByteString> getCustomTags() {
    return customTags;
  }

  ByteString getOutputTopic() {
    return outputTopic;
  }

  ByteString getOutputMessageType() {
    return outputMessageType;
  }

  Boolean isUnregistered() {
    return unregistered;
  }
}
