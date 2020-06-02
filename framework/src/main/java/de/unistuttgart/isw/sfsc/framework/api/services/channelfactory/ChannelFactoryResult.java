package de.unistuttgart.isw.sfsc.framework.api.services.channelfactory;

import de.unistuttgart.isw.sfsc.framework.api.services.pubsub.SfscPublisher;

public class ChannelFactoryResult {
  private final SfscPublisher sfscPublisher;
  private final Runnable onDeliverySuccess;
  private final Runnable onDeliveryFail;

  public ChannelFactoryResult(SfscPublisher sfscPublisher, Runnable onDeliverySuccess, Runnable onDeliveryFail) {
    this.sfscPublisher = sfscPublisher;
    this.onDeliverySuccess = onDeliverySuccess;
    this.onDeliveryFail = onDeliveryFail;
  }

  SfscPublisher getPublisher() {
    return sfscPublisher;
  }

  Runnable getOnDeliverySuccess() {
    return onDeliverySuccess;
  }

  Runnable getOnDeliveryFail() {
    return onDeliveryFail;
  }

}
