package de.unistuttgart.isw.sfsc.commonjava.util;

import com.google.protobuf.ByteString;

public final class StoreEvent {
  public enum StoreEventType{
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
}
