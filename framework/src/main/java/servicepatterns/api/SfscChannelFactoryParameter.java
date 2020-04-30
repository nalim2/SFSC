package servicepatterns.api;

import com.google.protobuf.ByteString;
import java.util.Map;

public class SfscChannelFactoryParameter {

  private String serviceName;
  private Map<String, ByteString> customTags;
  private ByteString inputTopic;
  private ByteString inputMessageType;


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
}
