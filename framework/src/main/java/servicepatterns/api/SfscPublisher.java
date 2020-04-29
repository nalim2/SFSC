package servicepatterns.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import java.util.Map;
import java.util.concurrent.Future;

public interface SfscPublisher extends NotThrowingAutoCloseable {

  Future<Void> unsubscriptionFuture();

  Handle onUnsubscription(Runnable runnable);

  void publish(Message payload);

  Map<String, ByteString> getTags();

  Future<Void> subscriptionFuture();

  Handle onSubscription(Runnable runnable);

  @Override
  void close();
}
