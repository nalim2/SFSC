package servicepatterns.api;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Subscriber;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import java.util.function.Consumer;
import servicepatterns.api.tagging.ServiceFactory;

final class SfscSubscriberImplementation implements SfscSubscriber {

  private final Runnable closeCallback;

  public SfscSubscriberImplementation(SfscServiceDescriptor publisherDescriptor, ServiceFactory serviceFactory,
      Consumer<ByteString> subscriberConsumer) {
    Subscriber subscriber = new Subscriber(
        serviceFactory.pubSubConnection(),
        subscriberConsumer,
        publisherDescriptor.getPublisherTags().getOutputTopic(),
        serviceFactory.executorService());
    closeCallback = subscriber::close;
  }

  @Override
  public void close() {
    closeCallback.run();
  }
}
