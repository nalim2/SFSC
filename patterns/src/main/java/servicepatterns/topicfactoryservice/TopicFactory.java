package servicepatterns.topicfactoryservice;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.patterns.SfscError;
import de.unistuttgart.isw.sfsc.patterns.publishergenerator.Reply;
import de.unistuttgart.isw.sfsc.patterns.publishergenerator.Request;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servicepatterns.SfscMessage;
import servicepatterns.pubsub.PubSubFactory;
import servicepatterns.pubsub.Publisher;

public class TopicFactory implements Function<SfscMessage, ByteString> {

  private static final Logger logger = LoggerFactory.getLogger("Publisher");
  private final Set<Publisher> publishers = new HashSet<>();
  private final Set<Publisher> publisherView = Collections.unmodifiableSet(publishers);
  private final PubSubFactory pubSubFactory;

  public TopicFactory(Adapter adapter) {
    pubSubFactory = new PubSubFactory(adapter);
  }

  public Set<Publisher> getPublishers() {
    return publisherView;
  }

  @Override
  public ByteString apply(SfscMessage sfscMessage) {
    if (sfscMessage.getError() == SfscError.NO_ERROR) {
      try {
        Request request = Request.parseFrom(sfscMessage.getPayload());
        Map<String, ByteString> tags = Map.ofEntries(Map.entry("REQUEST", request.toByteString()));

        ByteString topicByteString = ByteString.copyFromUtf8(UUID.randomUUID().toString());
        Publisher publisher = pubSubFactory.publisher(topicByteString, tags);

        publishers.add(publisher);
        return Reply.newBuilder().putAllTags(publisher.getTags()).build().toByteString();
      } catch (InvalidProtocolBufferException e) {
        logger.warn("received malformed request", e);
      }
    }
    return Reply.getDefaultInstance().toByteString(); //todo
  }
}
