package de.unistuttgart.isw.sfsc.commonjava.util;

public interface Handle extends NotThrowingAutoCloseable {

  @Override
  void close();
}
