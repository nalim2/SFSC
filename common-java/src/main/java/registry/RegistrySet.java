package registry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

public interface RegistrySet<T> {

  Set<T> getEntries();

  boolean contains(T entry);

  Set<T> getMatching(Predicate<? super T> predicate);

  void add(T entry);

  void addAll(Collection<? extends T> entry);

  void remove(T entry);

  void removeMatching(Predicate<? super T> predicate);
}
