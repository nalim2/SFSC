package registry;

import java.util.Set;
import java.util.function.Predicate;

public interface RegistrySet<T> {

  Set<T> getEntries();

  boolean contains(T entry);

  Set<T> getMatching(Predicate<T> predicate);

  void add(T entry);

  void remove(T entry);

  void removeMatching(Predicate<T> predicate);
}
