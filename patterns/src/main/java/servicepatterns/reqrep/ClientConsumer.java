package servicepatterns.reqrep;

import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.commonjava.registry.TimeoutRegistry;
import de.unistuttgart.isw.sfsc.patterns.SfscError;
import de.unistuttgart.isw.sfsc.patterns.reqrep.RequestReplyMessage;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servicepatterns.SfscMessage;
import servicepatterns.SfscMessageImpl;

class ClientConsumer implements Consumer<SfscMessage>, AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(ClientConsumer.class);
  private final TimeoutRegistry<Integer, Consumer<SfscMessage>> timeoutRegistry = new TimeoutRegistry<>();

  void addCallback(Integer id, Consumer<SfscMessage> consumer, int timeoutMs) {
    timeoutRegistry.put(id, consumer, timeoutMs, () -> consumer.accept(new SfscMessageImpl(SfscError.TIMEOUT, null)));
  }

  @Override
  public void accept(SfscMessage sfscMessage) {
    try {
      RequestReplyMessage response = RequestReplyMessage.parseFrom(sfscMessage.getPayload());
      timeoutRegistry
          .remove(response.getMessageId())
          .ifPresent(consumer -> consumer.accept(new SfscMessageImpl(sfscMessage.getError(), response.getPayload())));
    } catch (InvalidProtocolBufferException e) {
      logger.warn("Received malformed message", e);
    }
  }

  @Override
  public void close() {
    timeoutRegistry.close();
  }
}

