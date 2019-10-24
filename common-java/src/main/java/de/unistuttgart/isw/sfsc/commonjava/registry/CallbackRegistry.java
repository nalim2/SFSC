package de.unistuttgart.isw.sfsc.commonjava.registry;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import java.util.function.Consumer;

public class CallbackRegistry implements NotThrowingAutoCloseable {

  private final TimeoutRegistry<Integer, Consumer<ByteString>> registry = new TimeoutRegistry<>(); //todo inject executor, pass to timeout registry, also use for callbacks

  public void addCallback(int id, Consumer<ByteString> consumer, int timeoutMs, Runnable timeoutRunnable) {
    registry.put(id, consumer, timeoutMs, timeoutRunnable);
  }

  public void performCallback(int id, ByteString byteString) {
    registry
        .remove(id)
        .ifPresent(consumer ->
            consumer.accept(byteString)
        );
  }

  @Override
  public void close() {
    registry.close();
  }

}
