package servicepatterns.api;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServerTags.RegexDefinition;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServerTags.RegexDefinition.VarRegex.RegexCase;
import java.util.Map;
import java.util.Objects;

public class SfscServerParameter {

  private String serviceName;
  private Map<String, ByteString> customTags;
  private ByteString inputTopic;
  private ByteString inputMessageType;
  private ByteString outputMessageType;
  private RegexDefinition regexDefinition;
  private Integer timeoutMs;
  private Integer sendRateMs;
  private Integer sendMaxTries;

  public SfscServerParameter setServiceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  public SfscServerParameter setCustomTags(Map<String, ByteString> customTags) {
    this.customTags = customTags;
    return this;
  }

  public SfscServerParameter setInputTopic(ByteString inputTopic) {
    this.inputTopic = inputTopic;
    return this;
  }

  public SfscServerParameter setInputMessageType(ByteString inputMessageType) {
    this.inputMessageType = inputMessageType;
    return this;
  }

  public SfscServerParameter setOutputMessageType(ByteString outputMessageType) {
    this.outputMessageType = outputMessageType;
    return this;
  }

  public SfscServerParameter setRegexDefinition(RegexDefinition regexDefinition) {
    if (regexDefinition != null) {
      validateRegexDefinition(regexDefinition);
    }
    this.regexDefinition = regexDefinition;
    return this;
  }

  public SfscServerParameter setTimeoutMs(Integer timeoutMs) {
    if (sendMaxTries != null && sendMaxTries < 0) {
      throw new IllegalArgumentException();
    }
    this.timeoutMs = timeoutMs;
    return this;
  }

  public SfscServerParameter setSendRateMs(Integer sendRateMs) {
    if (sendMaxTries != null && sendMaxTries < 0) {
      throw new IllegalArgumentException();
    }
    this.sendRateMs = sendRateMs;
    return this;
  }

  public SfscServerParameter setSendMaxTries(Integer sendMaxTries) {
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

  ByteString getOutputMessageType() {
    return outputMessageType;
  }

  RegexDefinition getRegexDefinition() {
    return regexDefinition;
  }

  Integer getTimeoutMs() {
    return timeoutMs;
  }

  Integer getSendRateMs() {
    return sendRateMs;
  }

  Integer getSendMaxTries() {
    return sendMaxTries;
  }

  static void validateRegexDefinition(RegexDefinition regexDefinition) {
    regexDefinition.getRegexesList()
        .forEach(varRegex -> Objects.requireNonNull(varRegex.getVarName()));
    regexDefinition.getRegexesList()
        .stream()
        .filter(varRegex -> varRegex.getRegexCase() == RegexCase.STRING_REGEX)
        .forEach(varRegex -> Objects.requireNonNull(varRegex.getStringRegex()));
  }
}
