package de.unistuttgart.isw.sfsc.client.adapter.patterns.tags;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor.ServiceType;
import java.util.Map;
import java.util.function.Predicate;

public class FilterFactory {

  public static Predicate<Map<String, ByteString>> byteStringFilter(String key, Predicate<ByteString> predicate) {
    return new ByteStringFilter(key, predicate);
  }

  public static Predicate<Map<String, ByteString>> stringFilter(String key, Predicate<String> predicate) {
    return new StringFilter(key, predicate);
  }

  public static Predicate<Map<String, ByteString>> stringEqualsFilter(String key, String equalString) {
    return new StringFilter(key, equalString::equals);
  }

  public static Predicate<Map<String, ByteString>> servers() {
    return new TypeFilter(ServiceType.SERVER);
  }

  public static Predicate<Map<String, ByteString>> clients() {
    return new TypeFilter(ServiceType.CLIENT);
  }

  public static Predicate<Map<String, ByteString>> publishers() {
    return new TypeFilter(ServiceType.PUBLISHER);
  }

  public static Predicate<Map<String, ByteString>> subscribers() {
    return new TypeFilter(ServiceType.SUBSCRIBER);
  }
}
