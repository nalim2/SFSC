package de.unistuttgart.isw.sfsc.commonjava.util;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class Listeners<T> {

  private final Set<T> listeners = ConcurrentHashMap.newKeySet();

  public Handle add(T entry) {
    listeners.add(entry);
    return () -> listeners.remove(entry);
  }

  public void forEach(Consumer<T> action) {
    listeners.forEach(action);
  }

}
