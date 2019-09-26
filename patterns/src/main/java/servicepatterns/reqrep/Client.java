package servicepatterns.reqrep;

import com.google.protobuf.ByteString;
import java.util.Map;
import java.util.function.Consumer;
import servicepatterns.Service;
import servicepatterns.SfscMessage;

public interface Client extends Service {

  /**
   * Send new message.
   *
   * @param serverTags todo doc
   * @param payload data to send
   * @param consumer executed on receipt of response
   * @param timoutMs how long to wait for response
   */
  void send(Map<String, ByteString> serverTags, ByteString payload, Consumer<SfscMessage> consumer, int timoutMs);

}
