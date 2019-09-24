package servicepatterns;

import com.google.protobuf.ByteString;
import java.util.Map;
import java.util.function.Predicate;

public class FilterFactory {

  public static Predicate<Map<String, ByteString>> byteStringFilter(String key, Predicate<ByteString> predicate) {
    return tagMap -> {
      ByteString value = tagMap.get(key);
      return value != null && predicate.test(value);
    };
  }

  public static Predicate<Map<String, ByteString>> byteStringEqualsFilter(String key, ByteString equalByteString) {
    return byteStringFilter(key, equalByteString::equals);
  }

  public static Predicate<Map<String, ByteString>> stringFilter(String key, Predicate<String> predicate) {
    return byteStringFilter(key, byteString -> predicate.test(byteString.toStringUtf8()));
  }

  public static Predicate<Map<String, ByteString>> stringEqualsFilter(String key, String equalString) {
    return stringFilter(key, equalString::equals);
  }

}
