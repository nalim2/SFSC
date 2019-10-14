package servicepatterns.api.registry;

public interface Registration extends AutoCloseable {

  @Override
  void close();
}
