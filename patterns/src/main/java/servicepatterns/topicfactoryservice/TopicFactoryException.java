package servicepatterns.topicfactoryservice;

public class TopicFactoryException extends RuntimeException {

  public TopicFactoryException() {
    super();
  }

  public TopicFactoryException(String message) {
    super(message);
  }

  public TopicFactoryException(String message, Throwable cause) {
    super(message, cause);
  }

  public TopicFactoryException(Throwable cause) {
    super(cause);
  }
}
