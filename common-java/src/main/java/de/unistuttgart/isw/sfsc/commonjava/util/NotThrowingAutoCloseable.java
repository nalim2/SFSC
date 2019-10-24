package de.unistuttgart.isw.sfsc.commonjava.util;

public interface NotThrowingAutoCloseable extends AutoCloseable {

  @Override
  void close();
}
