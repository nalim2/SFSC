package servicepatterns.api;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import java.util.Map;
import java.util.concurrent.Future;

public interface SfscPublisher extends NotThrowingAutoCloseable {

  void publish(ByteString payload);

  Map<String, ByteString> getTags();

  Future<Void> subscriptionFuture();

  Future<Void> subscriptionFuture(Runnable runnable);

  @Override
  void close();
}
