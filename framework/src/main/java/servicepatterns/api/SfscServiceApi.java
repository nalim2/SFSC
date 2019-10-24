package servicepatterns.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.framework.descriptor.RegexDefinition;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import servicepatterns.basepatterns.ackreqrep.AckServerResult;

public interface SfscServiceApi {

  Set<Map<String, ByteString>> getServices();

  Set<Map<String, ByteString>> getServices(String name);

  Set<Map<String, ByteString>> getServices(String name, Message message, Collection<String> varPaths);

  SfscServer server(String name, ByteString inputMessageType, ByteString serverTopic, ByteString outputMessageType, RegexDefinition regexDefinition,
      Map<String, ByteString> customTags, Function<ByteString, AckServerResult> serverFunction, int timeoutMs, int sendRateMs, int sendMaxTries);

  SfscClient client();

  SfscPublisher publisher(String name, ByteString outputTopic, ByteString outputMessageType, Map<String, ByteString> customTags);

  SfscPublisher unregisteredPublisher(String name, ByteString outputTopic, ByteString outputMessageType, Map<String, ByteString> customTags);

  SfscSubscriber subscriber(Map<String, ByteString> publisherTags, Consumer<ByteString> consumer);

  SfscSubscriber subscriber(ByteString publisherTopic, Consumer<ByteString> consumer);

  SfscServer channelGenerator(String name, Map<String, ByteString> customTags,  ByteString serverTopic, ByteString inputMessageType,
      Function<ByteString, SfscPublisher> channelFactory);

}
