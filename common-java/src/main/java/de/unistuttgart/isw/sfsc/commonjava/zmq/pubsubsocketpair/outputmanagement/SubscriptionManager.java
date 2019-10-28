package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;

public interface SubscriptionManager {

  Handle subscribe(byte[] topic);

  Handle subscribe(ByteString topic);

  Handle subscribe(String topic);
}
