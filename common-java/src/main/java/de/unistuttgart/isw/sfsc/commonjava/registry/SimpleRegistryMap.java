package de.unistuttgart.isw.sfsc.commonjava.registry;

import com.google.protobuf.Message;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class SimpleRegistryMap<K, V> implements RegistryMap<K, V> {

  private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

  public static <K,V extends Message> RegistryMap<K,V> getInstance() {
    return new SimpleRegistryMap<>();
  }

  @Override
  public Collection<V> getEntries() {
    return Collections.unmodifiableCollection(map.values());
  }

  @Override
  public Optional<V> lookup(K key) {
    return Optional.ofNullable(map.get(key));
  }

  @Override
  public void put(K key, V value) {
    map.put(key, value);
  }

  @Override
  public Optional<V> remove(K key) {
    return Optional.ofNullable(map.remove(key));
  }

  @Override
  public boolean removeMatching(Predicate<V> predicate) {
    return map.values().removeIf(predicate);
  }
}
