package servicepatterns.api;

public interface SfscSubscriber extends AutoCloseable {

  @Override
  void close();

}
