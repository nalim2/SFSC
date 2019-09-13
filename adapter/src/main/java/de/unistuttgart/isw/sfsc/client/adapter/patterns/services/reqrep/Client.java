package de.unistuttgart.isw.sfsc.client.adapter.patterns.services.reqrep;

import de.unistuttgart.isw.sfsc.client.adapter.patterns.SfscMessage;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.Service;
import java.util.function.Consumer;

public interface Client extends Service {

  /**
   * Send new message.
   *
   * @param payload data to send
   * @param consumer executed on receipt of response
   * @param timoutMs how long to wait for response
   */
  void send(byte[] payload, Consumer<SfscMessage> consumer, int timoutMs);

}
