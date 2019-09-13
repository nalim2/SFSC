package de.unistuttgart.isw.sfsc.client.adapter.patterns.services;

import com.google.protobuf.ByteString;
import java.util.Collections;
import java.util.Map;

public class BaseService implements Service {

  private final Map<String, ByteString> tags;
  private final Runnable closer;

  public BaseService(Map<String, ByteString> tags, Runnable closer) {
    this.tags = tags;
    this.closer = closer;
  }

  @Override
  public Map<String, ByteString> getTags() {
    return Collections.unmodifiableMap(tags);
  }

  @Override
  public void close() {
    closer.run();
  }
}
