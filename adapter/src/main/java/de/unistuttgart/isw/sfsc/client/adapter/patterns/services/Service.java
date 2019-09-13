package de.unistuttgart.isw.sfsc.client.adapter.patterns.services;

import com.google.protobuf.ByteString;
import java.util.Map;

/**
 * Super interface for services.
 */
public interface Service extends AutoCloseable {

  /**
   * Returns registered tags.
   * @return tags
   */
  Map<String, ByteString> getTags();

  /**
   * Auto unregisters if registered.
   */
  @Override
  void close();
}
