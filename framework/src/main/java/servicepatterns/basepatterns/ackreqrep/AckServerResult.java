package servicepatterns.basepatterns.ackreqrep;

import com.google.protobuf.ByteString;

public final class AckServerResult {

  private final ByteString response;
  private final Runnable onDeliverySuccess;
  private final Runnable onDeliveryFail;

  public AckServerResult(ByteString response, Runnable onDeliverySuccess, Runnable onDeliveryFail) {
    this.response = response;
    this.onDeliverySuccess = onDeliverySuccess;
    this.onDeliveryFail = onDeliveryFail;
  }

  ByteString getResponse() {
    return response;
  }

  Runnable getOnDeliverySuccess() {
    return onDeliverySuccess;
  }

  Runnable getOnDeliveryFail() {
    return onDeliveryFail;
  }

}
