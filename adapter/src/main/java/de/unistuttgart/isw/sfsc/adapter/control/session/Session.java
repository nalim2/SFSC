package de.unistuttgart.isw.sfsc.adapter.control.session;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;

public interface Session extends NotThrowingAutoCloseable {

  WelcomeInformation getWelcomeInformation();

  @Override
  void close();
}
