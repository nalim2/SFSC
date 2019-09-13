package de.unistuttgart.isw.sfsc.client.adapter.patterns.tags;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor.ServiceType;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor.Tags;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TagCompleter {

  private final ByteString publisherType = ByteString.copyFromUtf8(ServiceType.PUBLISHER.name());
  private final ByteString subscriberType = ByteString.copyFromUtf8(ServiceType.SUBSCRIBER.name());
  private final ByteString serverType = ByteString.copyFromUtf8(ServiceType.SERVER.name());
  private final ByteString clientType = ByteString.copyFromUtf8(ServiceType.CLIENT.name());

  private final ByteString adapterId;
  private final ByteString coreId;

  public TagCompleter(String adapterId, String coreId) {
    this.adapterId = ByteString.copyFromUtf8(adapterId);
    this.coreId = ByteString.copyFromUtf8(coreId);
  }

  Map<String, ByteString> complete(Map<String, ByteString> tags, ByteString type) {
    Map<String, ByteString> safeCopy = new HashMap<>(tags);
    ByteString uuid = ByteString.copyFromUtf8(UUID.randomUUID().toString());
    safeCopy.put(Tags.SERVICE_ID.name(), uuid);
    safeCopy.put(Tags.ADAPTER_ID.name(), adapterId);
    safeCopy.put(Tags.CORE_ID.name(), coreId);
    safeCopy.put(Tags.TYPE.name(), type);
    safeCopy.putIfAbsent(Tags.TOPIC.name(), uuid);
    return safeCopy;
  }

  public Map<String, ByteString> completeServer(Map<String, ByteString> tags) {
    return complete(tags, serverType);
  }

  public Map<String, ByteString> completeClient(Map<String, ByteString> tags) {
    return complete(tags, clientType);
  }

  public Map<String, ByteString> completePublisher(Map<String, ByteString> tags) {
    return complete(tags, publisherType);
  }

  public Map<String, ByteString> completeSubscriber(Map<String, ByteString> tags) {
    return complete(tags, subscriberType);
  }

}
