package servicepatterns.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface SfscClient {

  void request(Map<String, ByteString> serverTags, Message payload, Consumer<ByteString> consumer, int timeoutMs, Runnable timeoutRunnable);

  void request(ByteString serverTopic, Message payload, Consumer<ByteString> consumer, int timeoutMs, Runnable timeoutRunnable);

  Future<SfscSubscriber> requestChannel(Map<String, ByteString> channelFactoryTags, ByteString payload, int timeoutMs, Consumer<ByteString> consumer);

  Future<SfscSubscriber> requestChannel(ByteString channelFactoryTopic, ByteString payload, int timeoutMs, Consumer<ByteString> consumer);
}
