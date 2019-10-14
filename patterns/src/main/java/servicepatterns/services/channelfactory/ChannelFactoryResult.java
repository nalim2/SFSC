package servicepatterns.services.channelfactory;

import servicepatterns.api.SfscPublisher;

public class ChannelFactoryResult {

  private final SfscPublisher publisher;
  private final Runnable onSubscription;

  public ChannelFactoryResult(SfscPublisher publisher, Runnable onSubscription) {
    this.publisher = publisher;
    this.onSubscription = onSubscription;
  }

  public SfscPublisher getPublisher() {
    return publisher;
  }

  public Runnable getOnSubscriptionRunnable() {
    return onSubscription;
  }
}
