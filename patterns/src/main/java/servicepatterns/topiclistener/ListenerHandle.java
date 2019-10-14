package servicepatterns.topiclistener;

public interface ListenerHandle extends AutoCloseable {

  @Override
  void close();
}
