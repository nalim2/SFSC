package servicepatterns;

import com.google.protobuf.ByteString;
import java.util.Map;

public class ServiceImpl implements Service {

  private final Map<String, ByteString> tags;
  private final Runnable closer;

  public ServiceImpl(Map<String, ByteString> tags, Runnable closer) {
    this.tags = tags;
    this.closer = closer;
  }

  @Override
  public Map<String, ByteString> getTags() {
    return tags;
  }

  @Override
  public void close() {
    closer.run();
  }
}
