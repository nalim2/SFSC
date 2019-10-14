package servicepatterns.api;

import com.google.protobuf.ByteString;
import java.util.Map;

public interface SfscPublisher extends AutoCloseable {

  void publish(ByteString payload);

  Map<String, ByteString> getTags();

  @Override
  void close();
}
