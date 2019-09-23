package de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.tags.TagCompleter;
import de.unistuttgart.isw.sfsc.client.adapter.raw.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import java.util.Map;

public class PublisherFactory {

  private final TagCompleter tagCompleter;
  private final OutputPublisher publisher;
  private final RegistryClient registryClient;

  public PublisherFactory(TagCompleter tagCompleter, OutputPublisher publisher, RegistryClient registryClient) {
    this.tagCompleter = tagCompleter;
    this.publisher = publisher;
    this.registryClient = registryClient;
  }

  public Publisher publisher(Map<String, ByteString> tags) {
    Map<String, ByteString> publisherTags = tagCompleter.completePublisher(tags);
    return new PublisherImpl(publisherTags, publisher, () -> registryClient.removeService(tags));
  }

}
