package de.unistuttgart.isw.sfsc.framework.api.services.pubsub;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Publisher;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import de.unistuttgart.isw.sfsc.commonjava.util.synchronizing.Awaitable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription.SubscriptionTracker;
import de.unistuttgart.isw.sfsc.framework.api.services.ServiceFactory;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.PublisherTags;
import java.util.Optional;

public final class SfscPublisherImplementation implements SfscPublisher {

  private static final boolean defaultRegistrationFlag = false;

  private final SfscServiceDescriptor descriptor;
  private final Publisher publisher;
  private final SubscriptionTracker subscriptionTracker;
  private final ByteString topic;
  private final byte[] topicCache;
  private final Scheduler scheduler;
  private final Runnable closeCallback;

  public SfscPublisherImplementation(SfscPublisherParameter parameter, ServiceFactory serviceFactory) {
    PubSubConnection pubSubConnection = serviceFactory.pubSubConnection();
    String serviceId = serviceFactory.createServiceId();
    descriptor = SfscServiceDescriptor.newBuilder()
        .setServiceId(serviceId)
        .setAdapterId(serviceFactory.adapterId())
        .setCoreId(serviceFactory.coreId())
        .setServiceName(Optional.ofNullable(parameter.getServiceName()).orElse(serviceId))
        .putAllCustomTags(Optional.ofNullable(parameter.getCustomTags()).orElseGet(serviceFactory::defaultCustomTags))
        .setPublisherTags(PublisherTags.newBuilder()
            .setOutputTopic(Optional.ofNullable(parameter.getOutputTopic()).orElseGet(serviceFactory::createTopic))
            .setOutputMessageType(Optional.ofNullable(parameter.getOutputMessageType()).orElseGet(serviceFactory::defaultType))
            .setUnregistered(Optional.ofNullable(parameter.isUnregistered()).orElse(defaultRegistrationFlag))
            .build())
        .build();

    Handle handle = descriptor.getPublisherTags().getUnregistered() ? null : serviceFactory.registerService(descriptor);
    closeCallback = handle != null ? handle::close : null;
    topic = descriptor.getPublisherTags().getOutputTopic();
    topicCache = topic.toByteArray();
    publisher = new Publisher(pubSubConnection);
    subscriptionTracker = pubSubConnection.subscriptionTracker();
    this.scheduler = serviceFactory.scheduler();
  }

  @Override
  public SfscServiceDescriptor getDescriptor() {
    return descriptor;
  }

  @Override
  public Handle onSubscription(Runnable runnable) {
    return subscriptionTracker.addOneShotSubscriptionListener(topic, () -> scheduler.execute(runnable));
  }

  @Override
  public Handle onUnsubscription(Runnable runnable) {
    return subscriptionTracker.addOneShotUnsubscriptionListener(topic, () -> scheduler.execute(runnable));
  }

  @Override
  public Awaitable subscriptionAwaitable() {
    return subscriptionTracker.addOneShotSubscriptionListener(topic);
  }

  @Override
  public Awaitable unsubscriptionAwaitable() {
    return subscriptionTracker.addOneShotUnsubscriptionListener(topic);
  }

  @Override
  public void publish(Message payload) {
    publisher.publish(topicCache, payload);
  }

  @Override
  public void close() {
    Optional.ofNullable(closeCallback).ifPresent(Runnable::run);
  }
}
