package de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub;

import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.Service;

public interface Publisher extends Service {

  /**
   * Send new message.
   * @param payload data to send
   */
  void publish(byte[] payload);

  @Override
  void close();
}
