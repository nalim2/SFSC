package servicepatterns.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.framework.descriptor.ServiceDescriptor;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StoreEventStreamConverter implements Consumer<StoreEvent> {

  private static final Logger logger = LoggerFactory.getLogger(StoreEventStreamConverter.class);

  private final Set<Map<String, ByteString>> services;

  StoreEventStreamConverter(Set<Map<String, ByteString>> services) {this.services = services;}

  public synchronized void accept(StoreEvent storeEvent) {
    try {
      Map<String, ByteString> service = ServiceDescriptor.parseFrom(storeEvent.getData()).getTagsMap();
      switch (storeEvent.getStoreEventType()) {
        case DELETE: {
          services.remove(service);
          break;
        }
        case CREATE: {
          services.add(service);
          break;
        }
        default: {
          logger.warn("Received unsupported store event with type {}", storeEvent.getStoreEventType());
          break;
        }
      }
    } catch (InvalidProtocolBufferException e) {
      logger.warn("Registry contains malformed entries", e);
    }
  }
}
