package servicepatterns.api;

final class SfscSubscriberImplementation implements SfscSubscriber {

  private final Runnable onClose;

  SfscSubscriberImplementation(Runnable onClose) {
    this.onClose = onClose;
  }

  @Override
  public void close() {
    onClose.run();
  }
}
