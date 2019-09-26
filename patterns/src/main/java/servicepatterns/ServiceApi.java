package servicepatterns;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.patterns.RegexDefinition;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import servicepatterns.pubsub.Publisher;
import servicepatterns.reqrep.Client;
import servicepatterns.topicfactoryservice.TopicFactoryService;

public interface ServiceApi {

  Set<Map<String, ByteString>> getServices();

  Set<Map<String, ByteString>> getServices(String name);

  Set<Map<String, ByteString>> getServices(String name, Message message, Collection<String> varPaths);

  Service server(String name, String inputMessageType, String outputMessageType, RegexDefinition regexDefinition,
      Map<String, ByteString> customTags, Function<SfscMessage, ByteString> serverFunction);

  Client client();

  Publisher addPublisher(String name, String outputMessageType, Map<String, ByteString> customTags);

  Service subscriber(Map<String, ByteString> publisherTags, Consumer<SfscMessage> consumer);

  TopicFactoryService addTopicGenerator(String name, Map<String, ByteString> customTags);

  Future<Map<String, ByteString>> requestTopic(Client client, Map<String, ByteString> topicGeneratorTags, int timeoutMs);
}
