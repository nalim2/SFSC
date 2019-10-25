package de.unistuttgart.isw.sfsc.commonjava.util;

import java.util.Set;
import java.util.stream.Collectors;

public final class StoreEvent<T> {

  public enum StoreEventType {
    CREATE, DELETE
  }

  private final StoreEventType storeEventType;
  private final T data;

  public StoreEvent(StoreEventType storeEventType, T data) {
    this.storeEventType = storeEventType;
    this.data = data;
  }

  public StoreEventType getStoreEventType() {
    return storeEventType;
  }

  public T getData() {
    return data;
  }

  public static <T> Set<StoreEvent<T>> toStoreEventSet(Set<T> set) {
    return Set.copyOf(set).stream()
        .map(entry -> new StoreEvent<>(StoreEventType.CREATE, entry))
        .collect(Collectors.toUnmodifiableSet());
  }
}
