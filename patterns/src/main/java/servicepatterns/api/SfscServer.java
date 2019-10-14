package servicepatterns.api;

import com.google.protobuf.ByteString;
import java.util.Map;

public interface SfscServer extends AutoCloseable {

  Map<String, ByteString> getTags();

  @Override
  void close();
}
