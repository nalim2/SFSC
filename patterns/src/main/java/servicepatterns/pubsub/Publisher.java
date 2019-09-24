package servicepatterns.pubsub;

import servicepatterns.Service;

public interface Publisher extends Service {

  /**
   * Send new message.
   * @param payload data to send
   */
  void publish(byte[] payload);

}
