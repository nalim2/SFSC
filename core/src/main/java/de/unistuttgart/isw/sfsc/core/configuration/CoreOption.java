package de.unistuttgart.isw.sfsc.core.configuration;

import java.util.Objects;
import java.util.function.Predicate;

public enum CoreOption implements Predicate<String> {

  HOST(any -> true),
  BACKEND_HOST(any -> true),
  HAZELCAST_PORT(any -> true),
  BACKEND_PORT(any -> true),
  CONTROL_PUB_PORT(any -> true),
  CONTROL_SUB_PORT(any -> true),
  DATA_PUB_PORT(any -> true),
  DATA_SUB_PORT(any -> true);

  private final Predicate<String> validator;

  CoreOption(Predicate<String> validator) {
    Objects.requireNonNull(validator);
    this.validator = validator;
  }

  @Override
  public boolean test(String value) {
    return validator.test(value);
  }
}
