package de.unistuttgart.isw.sfsc.adapter.control;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.command.CommandReply;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface RegistryApi {

  Future<CommandReply> create(SfscServiceDescriptor entry);

  Future<CommandReply> remove(SfscServiceDescriptor entry);

  Set<SfscServiceDescriptor> getEntries();

  Handle addListener(Consumer<StoreEvent<SfscServiceDescriptor>> listener);

}
