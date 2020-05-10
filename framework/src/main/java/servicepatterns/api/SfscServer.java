package servicepatterns.api;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;

public interface SfscServer extends NotThrowingAutoCloseable {

  SfscServiceDescriptor getDescriptor();

  @Override
  void close();
}
