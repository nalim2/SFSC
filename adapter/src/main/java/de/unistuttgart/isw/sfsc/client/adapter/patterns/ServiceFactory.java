package de.unistuttgart.isw.sfsc.client.adapter.patterns;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.Service;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub.Publisher;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub.Subscriber;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.reqrep.Client;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.reqrep.Server;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ServiceFactory {

  /**
   * Lists registered services.
   *
   * @return registered services
   */
  Future<Set<Map<String, ByteString>>> getServices();

  /**
   * Registers service.
   *
   * @param service service to register
   */
  void register(Service service);

  /**
   * Produces new publisher.
   *
   * @param tags name, messageTypeUrl, ..., everything to filter on
   * @return publisher instance
   */
  Publisher publisher(Map<String, ByteString> tags);

  /**
   * Produces new subscriber.
   *
   * @param publisherTags at least some mandatory tags: type for validation, topic, ...
   * @param consumer executed on receipt of message
   * @param executor executor the consumer is executed on
   * @return subscriber instance
   */
  Subscriber subscriber(Map<String, ByteString> publisherTags, Consumer<SfscMessage> consumer, Executor executor);

  /**
   * Produces new server.
   *
   * @param tags name, messageTypeUrl, ..., everything to filter on
   * @param server function executed on receipt of message, produces response
   * @param executor executor the function is executed on
   * @return server instance
   */
  Server server(Map<String, ByteString> tags, Function<SfscMessage, byte[]> server, Executor executor);

  /**
   * Produces new Client.
   *
   * @param serverTags at least some mandatory tags: type for validation, topic, ...
   * @return client instance
   */
  Client client(Map<String, ByteString> serverTags, Executor executor);

}
