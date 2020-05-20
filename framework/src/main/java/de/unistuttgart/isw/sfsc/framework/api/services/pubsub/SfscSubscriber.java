package de.unistuttgart.isw.sfsc.framework.api.services.pubsub;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;

public interface SfscSubscriber extends NotThrowingAutoCloseable {

  @Override
  void close();

}
