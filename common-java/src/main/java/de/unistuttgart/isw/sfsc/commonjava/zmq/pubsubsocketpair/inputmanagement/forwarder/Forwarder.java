package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.forwarder;

import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import java.util.List;
import java.util.function.Consumer;

public interface Forwarder {

  Handle addListener(Consumer<List<byte[]>> sink);
}
