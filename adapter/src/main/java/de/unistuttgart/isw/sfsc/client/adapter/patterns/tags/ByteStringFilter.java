package de.unistuttgart.isw.sfsc.client.adapter.patterns.tags;

import com.google.protobuf.ByteString;
import java.util.Map;
import java.util.function.Predicate;

class ByteStringFilter implements Predicate<Map<String, ByteString>> {

  private final String key;
  private final Predicate<ByteString> predicate;

  ByteStringFilter(String key, Predicate<ByteString> valuePredicate) {
    this.key = key;
    this.predicate = valuePredicate;
  }

  @Override
  public boolean test(Map<String, ByteString> tagMap) {
    ByteString value = tagMap.get(key);
    return value != null && predicate.test(value);
  }
}
