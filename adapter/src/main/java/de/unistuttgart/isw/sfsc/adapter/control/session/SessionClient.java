package de.unistuttgart.isw.sfsc.adapter.control.session;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Hello;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Welcome;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleClient;
import de.unistuttgart.isw.sfsc.commonjava.util.FutureAdapter;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

final class SessionClient {

  private final SimpleClient client;
  private final ByteString serverTopic;
  private final int timeoutMs;

  SessionClient(SimpleClient client, ByteString serverTopic, int timeoutMs) {
    this.client = client;
    this.serverTopic = serverTopic;
    this.timeoutMs = timeoutMs;
  }

  Future<Welcome> sendHello(String adapterId) {
    ByteString hello = Hello.newBuilder().setAdapterId(adapterId).build().toByteString();
    FutureAdapter<ByteString, Welcome> future = new FutureAdapter<>(Welcome::parseFrom, () -> {throw new TimeoutException();});
    client.send(serverTopic, hello, future::handleInput, timeoutMs, future::handleError);
    return future;
  }

}
