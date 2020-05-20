package de.unistuttgart.isw.sfsc.framework.api.services.channelfactory;

public class ChannelFactoryException extends RuntimeException {

  public ChannelFactoryException() {
    super();
  }

  public ChannelFactoryException(String message) {
    super(message);
  }

  public ChannelFactoryException(String message, Throwable cause) {
    super(message, cause);
  }

  public ChannelFactoryException(Throwable cause) {
    super(cause);
  }
}
