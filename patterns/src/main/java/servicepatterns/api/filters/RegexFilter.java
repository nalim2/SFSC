package servicepatterns.api.filters;


import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.patterns.tags.RegexDefinition;
import de.unistuttgart.isw.sfsc.patterns.tags.RegexDefinition.VarRegex.NumberRegex;
import de.unistuttgart.isw.sfsc.patterns.tags.RegexDefinition.VarRegex.StringRegex;
import de.unistuttgart.isw.sfsc.patterns.tags.ServerTags;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RegexFilter implements Predicate<Map<String, ByteString>> {

  private static final Logger logger = LoggerFactory.getLogger(RegexFilter.class);
  private final Message message;
  private final String varPath;

  RegexFilter(Message message, String varPath) {
    this.message = message;
    this.varPath = varPath;
  }

  @Override
  public boolean test(Map<String, ByteString> service) {
    try {
      ByteString regexDefinitionByteString = service.get(ServerTags.SFSC_SERVER_REGEX.name());
      if (regexDefinitionByteString != null) {
        RegexDefinition regexDefinition = RegexDefinition.parseFrom(regexDefinitionByteString);
        return test(regexDefinition);
      } else {
        return false;
      }
    } catch (InvalidProtocolBufferException e) {
      logger.warn("malformed regex", e);
      return false;
    }
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
