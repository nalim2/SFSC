package servicepatterns.api.filtering;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

public final class Filters {

  private Filters() {}

  public static Predicate<Map<String, ByteString>> byteStringFilter(String key, Predicate<ByteString> predicate) {
    return new ByteStringFilter(key, predicate);
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

  public static Predicate<Map<String, ByteString>> regexFilter(Message message, String varPath) {
    return new RegexFilter(message, varPath);
  }

  public static Predicate<Map<String, ByteString>> regexFilter(Message message, Collection<String> varPaths) {
    return varPaths
        .stream()
        .map(varPath -> regexFilter(message, varPath))
        .reduce(Predicate::and)
        .orElseThrow();
  }
}
