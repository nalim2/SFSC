package de.unistuttgart.isw.sfsc.commonjava.util;

import com.google.protobuf.ByteString;
import java.util.Set;
import java.util.stream.Collectors;

public final class StoreEvent {

  public enum StoreEventType {
    CREATE, DELETE
  }

  private final StoreEventType storeEventType;
  private final ByteString data;

  public StoreEvent(StoreEventType storeEventType, ByteString data) {
    this.storeEventType = storeEventType;
    this.data = data;
  }

  public StoreEventType getStoreEventType() {
    return storeEventType;
  }

  public ByteString getData() {
    return data;
  }

  public static Set<StoreEvent> toStoreEventSet(Set<ByteString> set) {
    return Set.copyOf(set).stream()
        .map(entry -> new StoreEvent(StoreEventType.CREATE, entry))
        .collect(Collectors.toUnmodifiableSet());
  }
}
