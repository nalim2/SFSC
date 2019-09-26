package servicepatterns.pubsub;

import com.google.protobuf.ByteString;
import servicepatterns.Service;

public interface Publisher extends Service {

  /**
   * Send new message.
   * @param payload data to send
   */
  void publish(ByteString payload);

}
