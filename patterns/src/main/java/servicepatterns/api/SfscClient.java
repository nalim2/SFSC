package servicepatterns.api;

import com.google.protobuf.ByteString;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface SfscClient {

  Future<SfscSubscriber> requestChannel(Map<String, ByteString> channelFactoryTags, ByteString payload, int timeoutMs, Runnable timeoutRunnable,
      Consumer<ByteString> consumer);

  Future<SfscSubscriber> requestChannel(ByteString channelFactoryTopic, ByteString payload, int timeoutMs, Runnable timeoutRunnable,
      Consumer<ByteString> consumer);

  void request(Map<String, ByteString> serverTags, ByteString payload, Consumer<ByteString> consumer, int timeoutMs, Runnable timeoutRunnable);

  void request(ByteString serverTopic, ByteString payload, Consumer<ByteString> consumer, int timeoutMs, Runnable timeoutRunnable);

}
