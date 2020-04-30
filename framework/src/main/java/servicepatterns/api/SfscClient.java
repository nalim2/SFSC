package servicepatterns.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface SfscClient {

  void request(SfscServiceDescriptor serverDescriptor, Message payload, Consumer<ByteString> consumer, int timeoutMs, Runnable timeoutRunnable);

  Future<SfscSubscriber> requestChannel(SfscServiceDescriptor channelFactoryDescriptor, ByteString payload, int timeoutMs, Consumer<ByteString> consumer);
}
