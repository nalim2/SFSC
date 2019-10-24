package de.unistuttgart.isw.sfsc.benchmark.io;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.benchmark.BenchmarkMessage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

class MessageSupplier implements Supplier<ByteString> {

  private final Supplier<Long> idGenerator = new AtomicLong(1)::getAndIncrement;
  private final int messageSizeBytes;

  MessageSupplier(int messageSizeBytes) {
    this.messageSizeBytes = messageSizeBytes;
  }

  @Override
  public ByteString get() {
    return message(messageSizeBytes);
  }

  ByteString message(int messageSizeBytes) {
    final byte[] weightBytes = new byte[messageSizeBytes];
    ThreadLocalRandom.current().nextBytes(weightBytes);
    final ByteString weightByteString = ByteString.copyFrom(weightBytes);
    return BenchmarkMessage.newBuilder()
        .setWeight(weightByteString)
        .setId(idGenerator.get())
        .setSendTimestamp(System.nanoTime())
        .setServerTimestamp(Long.MAX_VALUE)
        .setReceiveTimestamp(Long.MAX_VALUE)
        .build()
        .toByteString();
  }

}
