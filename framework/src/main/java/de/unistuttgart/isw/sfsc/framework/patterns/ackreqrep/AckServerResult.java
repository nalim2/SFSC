package de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep;

import com.google.protobuf.Message;

public final class AckServerResult {

  private final Message response;
  private final Runnable onDeliverySuccess;
  private final Runnable onDeliveryFail;

  public AckServerResult(Message response, Runnable onDeliverySuccess, Runnable onDeliveryFail) {
    this.response = response;
    this.onDeliverySuccess = onDeliverySuccess;
    this.onDeliveryFail = onDeliveryFail;
  }

  Message getResponse() {
    return response;
  }

  Runnable getOnDeliverySuccess() {
    return onDeliverySuccess;
  }

  Runnable getOnDeliveryFail() {
    return onDeliveryFail;
  }

}
