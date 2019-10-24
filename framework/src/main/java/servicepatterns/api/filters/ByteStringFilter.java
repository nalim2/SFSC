package servicepatterns.api.filters;

import com.google.protobuf.ByteString;
import java.util.Map;
import java.util.function.Predicate;

final class ByteStringFilter implements Predicate<Map<String, ByteString>> {

  private final String key;
  private final Predicate<ByteString> predicate;

  ByteStringFilter(String key, Predicate<ByteString> predicate) {
    this.key = key;
    this.predicate = predicate;
  }

  @Override
  public boolean test(Map<String, ByteString> tags) {
    ByteString value = tags.get(key);
    return value != null && predicate.test(value);
  }
}
