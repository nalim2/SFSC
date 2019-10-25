package de.unistuttgart.isw.sfsc.commonjava.util;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent.StoreEventType;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class ReplayingListener implements Consumer<StoreEvent> {

  private final AtomicBoolean ready = new AtomicBoolean();
  private final Deque<StoreEvent> deque = new ConcurrentLinkedDeque<>();
  private final Consumer<StoreEvent> listener;

  public ReplayingListener(Consumer<StoreEvent> listener) {this.listener = listener;}

  public void prepend(Set<ByteString> prepopulationSnapshot) {
    prependEvents(StoreEvent.toStoreEventSet(prepopulationSnapshot));
  }

  public void prependEvents(Set<StoreEvent> prepopulationSnapshot) {
    Set<StoreEvent> prepopulation = Set.copyOf(prepopulationSnapshot);
    // events happening in the meantime can cause two problems:
    // 1. add event -> duplicated adds in stream
    // 2. remove event -> removing of not existing element

    //if we have duplicated adds, we delete them here
    deque.removeAll(prepopulation);
    //now handle already executed removals -> this is the case for not existing adds in our snapshot
    deque.removeIf(event ->
        event.getStoreEventType() == StoreEventType.DELETE
            && prepopulation.stream().noneMatch(new StoreEvent(StoreEventType.CREATE, event.getData())::equals));
    //now prepend our snapshot
    prepopulation.forEach(deque::addFirst);
  }

  public void start() {
    ready.set(true);
    processDeque();
  }

  @Override
  public void accept(StoreEvent storeEvent) {
    deque.add(storeEvent);
    processDeque();
  }

  synchronized void processDeque() {
    if (ready.get()) {
      StoreEvent element;
      while ((element = deque.poll()) != null) {
        listener.accept(element);
      }
    }
  }

}
