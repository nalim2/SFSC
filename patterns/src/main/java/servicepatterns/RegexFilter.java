package servicepatterns;


import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.patterns.Advanced_Tags;
import de.unistuttgart.isw.sfsc.patterns.RegexDefinition;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RegexFilter {

  private static final Logger logger = LoggerFactory.getLogger(RegexFilter.class);

  Set<Map<String, ByteString>> findMatching(Set<Map<String, ByteString>> services, Message message, Collection<String> varPaths) {

    return services.stream()
        .filter(service -> {
          try {
            RegexDefinition regexDefinition = RegexDefinition.parseFrom(service.get(Advanced_Tags.REGEX.name()));
            return varPaths.stream().allMatch(varPath ->
                regexDefinition.getRegexesList()
                    .stream()
                    .filter(varRegex -> varRegex.getVarName().equals(varPath))
                    .anyMatch(varRegex -> {
                          switch (varRegex.getRegexCase()) {
                            case NUMBER_REGEX: {
                              long value = (long) getValue(message, varPath);
                              return varRegex.getNumberRegex().getLowerBound() <= value
                                  && value < varRegex.getNumberRegex().getUpperBound();
                            }
                            case STRING_REGEX: {
                              String value = (String) getValue(message, varPath);
                              return value.matches(varRegex.getStringRegex().getRegex());
                            }
                            default: {
                              return false;
                            }
                          }
                        }
                    )
            );
          } catch (InvalidProtocolBufferException e) {
            logger.warn("malformed regex", e);
            return false;
          }
        })
        .collect(Collectors.toUnmodifiableSet());
  }

  Object getValue(Message message, String fieldPath) {
    String[] parts = fieldPath.split("\\.");
    Object subMessageObject = message;
    for (String part : parts) {
      Message subMessage = (Message) subMessageObject;
      FieldDescriptor fieldDescriptor = subMessage.getDescriptorForType().findFieldByName(part);
      subMessageObject = subMessage.getField(fieldDescriptor);
    }
    return subMessageObject;
  }
}
