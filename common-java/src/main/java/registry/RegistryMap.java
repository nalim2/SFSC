package registry;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public interface RegistryMap<K, V> {

  Collection<V> getEntries();
  Optional<V> lookup(K key);
  void put(K key, V value);
  Optional<V> remove(K key);
  boolean removeMatching(Predicate<V> predicate);
}
