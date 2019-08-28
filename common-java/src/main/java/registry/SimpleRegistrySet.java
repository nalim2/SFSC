package registry;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SimpleRegistrySet<T> implements RegistrySet<T> {

  private final Set<T> set = ConcurrentHashMap.newKeySet();

  public static <T> SimpleRegistrySet<T> getInstance() {
    return new SimpleRegistrySet<>();
  }

  @Override
  public Set<T> getEntries() {
    return Collections.unmodifiableSet(set);
  }

  @Override
  public boolean contains(T entry) {
    return set.contains(entry);
  }

  @Override
  public Set<T> getMatching(Predicate<? super T> predicate) {
    return set.stream().filter(predicate).collect(Collectors.toSet());
  }

  @Override
  public void add(T entry) {
    set.add(entry);
  }

  @Override
  public void addAll(Collection<? extends T> entry) {
    set.addAll(entry);
  }

  @Override
  public void remove(T entry) {
    set.remove(entry);
  }

  @Override
  public void removeMatching(Predicate<? super T> predicate) {
    set.removeIf(predicate);
  }

}
