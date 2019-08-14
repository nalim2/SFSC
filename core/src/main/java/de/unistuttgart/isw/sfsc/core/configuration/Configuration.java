package de.unistuttgart.isw.sfsc.core.configuration;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;

public class Configuration<T extends Enum<T> & Predicate<String>> {

  private final Map<T, String> options;

  Configuration(Map<T, String> options, Class<T> enumType) {
    //take Map instead of enum map in constructor
    //possible IllegalArgumentException if empty map is used in EnumMap Constructor
    //therefore use class in constructor because we have it anyway for Completeness Validation
    //then fill in next step
    EnumMap<T, String> enumMap = new EnumMap<>(enumType);
    enumMap.putAll(options);
    this.options = Collections.unmodifiableMap(enumMap);
    validateCompleteness(this.options, enumType);
    validateEntries(this.options);
  }

  public static <T extends Enum<T> & Predicate<String>> Configuration<T> create(Map<T, String> options, Class<T> enumType){
    return new Configuration<>(options, enumType);
  }

  static <T extends Enum<T>> void validateCompleteness(Map<T, String> options, Class<T> clazz) {
    if (options.size() != clazz.getEnumConstants().length) {
      throw new IllegalArgumentException("Given options incomplete");
    }
  }

  static <T extends Predicate<String>> void validateEntries(Map<T, String> options) {
    options.forEach((key, value) -> {
      if (!key.test(value)) {
        throw new IllegalArgumentException("key " + key + "has invalid value " + value);
      }
    });
  }

  public String get(T t) {
    return options.get(t);
  }

}
