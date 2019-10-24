package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Outbox;

public interface SubscriptionManager {

  Handle subscribe(ByteString topic);

  Handle subscribe(String topic);

  Outbox outbox();
}
