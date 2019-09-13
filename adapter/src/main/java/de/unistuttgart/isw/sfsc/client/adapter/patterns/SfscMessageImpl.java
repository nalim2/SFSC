package de.unistuttgart.isw.sfsc.client.adapter.patterns;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.patterns.SfscError;

public class SfscMessageImpl implements SfscMessage {

  private final SfscError error;
  private final ByteString payload;

  public SfscMessageImpl(SfscError error, ByteString payload) {
    this.error = error;
    this.payload = payload;
  }

  @Override
  public SfscError getError() {
    return error;
  }

  @Override
  public ByteString getPayload() {
    return payload;
  }
}
