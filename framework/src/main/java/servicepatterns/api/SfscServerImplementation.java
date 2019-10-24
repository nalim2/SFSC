package servicepatterns.api;

import com.google.protobuf.ByteString;
import java.util.Collections;
import java.util.Map;

final class SfscServerImplementation implements SfscServer {

  private final Map<String, ByteString> tags;
  private final Runnable onClose;

  SfscServerImplementation(Map<String, ByteString> tags, Runnable onClose) {
    this.tags = Collections.unmodifiableMap(tags);
    this.onClose = onClose;
  }

  @Override
  public Map<String, ByteString> getTags() {
    return tags;
  }

  @Override
  public void close() {
    onClose.run();
  }
}
