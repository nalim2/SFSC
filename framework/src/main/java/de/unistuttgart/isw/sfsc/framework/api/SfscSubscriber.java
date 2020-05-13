package de.unistuttgart.isw.sfsc.framework.api;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;

public interface SfscSubscriber extends NotThrowingAutoCloseable {

  @Override
  void close();

}
