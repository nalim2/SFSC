package de.unistuttgart.isw.sfsc.client.adapter.patterns;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.patterns.SfscError;

/**
 * Message to be consumed.
 */
public interface SfscMessage {

  /**
   * Returns the error field.
   * @return error field
   */
  SfscError getError();

  /**
   * Returns the payload field.
   * @return payload field
   */
  ByteString getPayload();

}
