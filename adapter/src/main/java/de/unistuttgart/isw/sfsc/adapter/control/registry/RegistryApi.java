package de.unistuttgart.isw.sfsc.adapter.control.registry;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.command.CommandReply;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface RegistryApi {

  Future<CommandReply> create(ByteString entry);

  Future<CommandReply> remove(ByteString entry);

  Set<ByteString> getEntries();

  Handle addListener(Consumer<StoreEvent<ByteString>> listener);

}
