package de.unistuttgart.isw.sfsc.framework.api.registry;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServiceTags.ServerTags.RegexDefinition;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServiceTags.ServerTags.RegexDefinition.VarRegex.NumberRegex;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServiceTags.ServerTags.RegexDefinition.VarRegex.StringRegex;
import java.util.Objects;
import java.util.function.Predicate;

final class RegexFilter implements Predicate<SfscServiceDescriptor> {

  private final Message message;
  private final String varPath;

  RegexFilter(Message message, String varPath) {
    this.message = message;
    this.varPath = varPath;
  }

  @Override
  public boolean test(SfscServiceDescriptor descriptor) {
    RegexDefinition regexDefinition = descriptor.getServiceTags().getServerTags().getRegex();
    return test(regexDefinition);
  }

  public boolean test(RegexDefinition regexDefinition) {
    return regexDefinition
        .getRegexesList()
        .stream()
        .filter(Objects::nonNull)
        .filter(varRegex -> varPath.equals(varRegex.getVarName()))
        .anyMatch(varRegex -> {
              switch (varRegex.getRegexCase()) {
                case NUMBER_REGEX: {
                  long value = (long) getFieldValue(message, varPath);
                  return check(value, varRegex.getNumberRegex());
                }
                case STRING_REGEX: {
                  String value = (String) getFieldValue(message, varPath);
                  return check(value, varRegex.getStringRegex());
                }
                default: {
                  return false;
                }
              }
            }
        );
  }

  Object getFieldValue(Message message, String fieldPath) {
    String[] parts = fieldPath.split("\\.");
    Object subMessageObject = message;
    for (String part : parts) {
      Message subMessage = (Message) subMessageObject;
      FieldDescriptor fieldDescriptor = subMessage.getDescriptorForType().findFieldByName(part);
      subMessageObject = subMessage.getField(fieldDescriptor);
    }
    return subMessageObject;
  }

  boolean check(String value, StringRegex stringRegex) {
    if (value != null && stringRegex != null && stringRegex.getRegex() != null) {
      return value.matches(stringRegex.getRegex());
    } else {
      return false;
    }
  }

  boolean check(long value, NumberRegex numberRegex) {
    return numberRegex != null
        && numberRegex.getLowerBound() <= value
        && value < numberRegex.getUpperBound();
  }

}
