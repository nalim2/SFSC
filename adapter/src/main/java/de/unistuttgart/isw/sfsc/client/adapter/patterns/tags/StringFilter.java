package de.unistuttgart.isw.sfsc.client.adapter.patterns.tags;

import com.google.protobuf.ByteString;
import java.util.Map;
import java.util.function.Predicate;

public class StringFilter implements Predicate<Map<String, ByteString>> {

  private final Predicate<Map<String, ByteString>> predicate;

  StringFilter(String key, Predicate<String> stringPredicate) {
    predicate = FilterFactory.byteStringFilter(key, byteString -> stringPredicate.test(byteString.toStringUtf8()));
  }

  @Override
  public boolean test(Map<String, ByteString> tags) {
    return predicate.test(tags);
  }
}
