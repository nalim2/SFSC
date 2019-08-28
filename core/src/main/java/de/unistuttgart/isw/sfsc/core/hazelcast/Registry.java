package de.unistuttgart.isw.sfsc.core.hazelcast;

import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor;
import java.util.Set;

public interface Registry {

  void create(ServiceDescriptor serviceDescriptor);

  Set<ServiceDescriptor> read();

  void delete(ServiceDescriptor serviceDescriptor);

}
