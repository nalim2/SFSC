package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.data;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public interface DataMultiplexer {

  Handle add(Predicate<ByteString> filter, BiConsumer<ByteString, ByteString> handler);
}
