package servicepatterns.api.tagging;


import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.framework.descriptor.BaseTags;
import de.unistuttgart.isw.sfsc.framework.descriptor.ChannelFactoryTags;
import de.unistuttgart.isw.sfsc.framework.descriptor.PublisherTags;
import de.unistuttgart.isw.sfsc.framework.descriptor.RegexDefinition;
import de.unistuttgart.isw.sfsc.framework.descriptor.RegexDefinition.VarRegex.RegexCase;
import de.unistuttgart.isw.sfsc.framework.descriptor.ServerTags;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Tagger {

  private Tagger() {
  }

  public static Map<String, ByteString> createPublisherTags(String name, String serviceId, String adapterId, String coreId,
      ByteString outputTopic, ByteString outputMessageType,
      Map<String, ByteString> customTags) {
    Map<String, ByteString> map = new HashMap<>();
    map.putAll(customTags);
    map.putAll(baseTags(name, serviceId, adapterId, coreId, SfscServiceType.PUBLISHER));
    putIfValid(map, PublisherTags.SFSC_PUBLISHER_OUTPUT_TOPIC.name(), outputTopic);
    putIfValid(map, PublisherTags.SFSC_PUBLISHER_OUTPUT_MESSAGE_TYPE.name(), outputMessageType);
    return Collections.unmodifiableMap(map);
  }

  public static Map<String, ByteString> createServerTags(
      String name, String serviceId, String adapterId, String coreId,
      ByteString inputTopic, ByteString inputMessageType, ByteString outputMessageType, RegexDefinition regexDefinition,
      Map<String, ByteString> customTags) {
    Map<String, ByteString> map = new HashMap<>();
    map.putAll(customTags);
    map.putAll(baseTags(name, serviceId, adapterId, coreId, SfscServiceType.SERVER));
    putIfValid(map, ServerTags.SFSC_SERVER_INPUT_TOPIC.name(), inputTopic);
    putIfValid(map, ServerTags.SFSC_SERVER_INPUT_MESSAGE_TYPE.name(), inputMessageType);
    putIfValid(map, ServerTags.SFSC_SERVER_OUTPUT_MESSAGE_TYPE.name(), outputMessageType);
    putIfValid(map, ServerTags.SFSC_SERVER_REGEX.name(), regexDefinition);
    return Collections.unmodifiableMap(map);
  }

  public static Map<String, ByteString> createChannelFactoryTags(String name, String serviceId, String adapterId, String coreId,
      ByteString inputTopic, ByteString inputMessageType,
      Map<String, ByteString> customTags) {
    Map<String, ByteString> map = new HashMap<>();
    map.putAll(customTags);
    map.putAll(baseTags(name, serviceId, adapterId, coreId, SfscServiceType.CHANNEL_FACTORY));
    putIfValid(map, ChannelFactoryTags.SFSC_CHANNEL_FACTORY_INPUT_TOPIC.name(), inputTopic);
    putIfValid(map, ChannelFactoryTags.SFSC_CHANNEL_FACTORY_INPUT_MESSAGE_TYPE.name(), inputMessageType);
    return Collections.unmodifiableMap(map);
  }

  public static Map<String, ByteString> baseTags(String name, String serviceId, String adapterId, String coreId, SfscServiceType serviceType) {
    return Map.ofEntries(
        Map.entry(BaseTags.SFSC_SERVICE_NAME.name(), ByteString.copyFromUtf8(name)),
        Map.entry(BaseTags.SFSC_SERVICE_ID.name(), ByteString.copyFromUtf8(serviceId)),
        Map.entry(BaseTags.SFSC_ADAPTER_ID.name(), ByteString.copyFromUtf8(adapterId)),
        Map.entry(BaseTags.SFSC_CORE_ID.name(), ByteString.copyFromUtf8(coreId)),
        Map.entry(BaseTags.SFSC_SERVICE_TYPE.name(), ByteString.copyFromUtf8(serviceType.name()))
    );
  }

  static <K> void putIfValid(Map<K, ByteString> map, K key, RegexDefinition value) {
    if (value != null) {
      validateRegexDefinition(value);
      putIfValid(map, key, value.toByteString());
    }
  }

  static <K> void putIfValid(Map<K, ByteString> map, K key, String value) {
    if (value != null) {
      putIfValid(map, key, ByteString.copyFromUtf8(value));
    }
  }

  static <K, V> void putIfValid(Map<K, V> map, K key, V value) {
    if (value != null) {
      map.put(key, value);
    }
  }

  static void validateRegexDefinition(RegexDefinition regexDefinition) {
    regexDefinition.getRegexesList()
        .forEach(varRegex -> Objects.requireNonNull(varRegex.getVarName(), "varName null"));
    regexDefinition.getRegexesList()
        .stream()
        .filter(varRegex -> varRegex.getRegexCase() == RegexCase.STRING_REGEX)
        .forEach(varRegex -> Objects.requireNonNull(varRegex.getStringRegex(), "stringRegEx null"));
  }

  public static String getName(Map<String, ByteString> tags){
    return tags.get(BaseTags.SFSC_SERVICE_NAME.name()).toStringUtf8();
  } //todo more getters?
}
