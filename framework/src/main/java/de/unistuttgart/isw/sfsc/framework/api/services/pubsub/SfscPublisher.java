package de.unistuttgart.isw.sfsc.framework.api.services.pubsub;

import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.synchronizing.Awaitable;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;


public interface SfscPublisher extends NotThrowingAutoCloseable {

  void publish(Message payload);

  Handle onSubscription(Runnable runnable);

  Handle onUnsubscription(Runnable runnable);

  Awaitable subscriptionAwaitable();

  Awaitable unsubscriptionAwaitable();

  SfscServiceDescriptor getDescriptor();

  @Override
  void close();
}
