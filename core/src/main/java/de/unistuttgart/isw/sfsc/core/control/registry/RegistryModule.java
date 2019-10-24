package de.unistuttgart.isw.sfsc.core.control.registry;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.core.hazelcast.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryModule implements NotThrowingAutoCloseable {

  private static final String QUERY_SERVER_TOPIC = "REGISTRY_QUERY_SERVER";
  private static final String COMMAND_SERVER_TOPIC = "REGISTRY_COMMAND_SERVER";
  private static final String EVENT_PUBLISHER_TOPIC = "REGISTRY_EVENT_PUBLISHER";
  private static final int REGISTRY_THREAD_NUMBER = 1;

  private static final Logger logger = LoggerFactory.getLogger(RegistryModule.class);

  private final ExecutorService executorService = Executors.unconfigurableExecutorService(
      Executors.newFixedThreadPool(REGISTRY_THREAD_NUMBER, new ExceptionLoggingThreadFactory(getClass().getName(), logger)));

  private final ByteString queryServerTopic = ByteString.copyFromUtf8(QUERY_SERVER_TOPIC);
  private final ByteString commandServerTopic = ByteString.copyFromUtf8(COMMAND_SERVER_TOPIC);
  private final ByteString eventPublisherTopic = ByteString.copyFromUtf8(EVENT_PUBLISHER_TOPIC);

  private final QueryServer queryServer;
  private final CommandServer commandServer;
  private final EventPublisher eventPublisher;

  RegistryModule(PubSubConnection pubSubConnection, Registry registry) {
    eventPublisher = new EventPublisher(pubSubConnection, eventPublisherTopic);
    registry.addEventListener(eventPublisher::send);
    commandServer = new CommandServer(pubSubConnection, commandServerTopic, executorService, registry);
    queryServer = new QueryServer(pubSubConnection, queryServerTopic, executorService, registry);
  }

  public static RegistryModule create(PubSubConnection pubSubConnection,  Registry registry) {
    return new RegistryModule(pubSubConnection, registry);
  }


  @Override
  public void close() {
    queryServer.close();
    commandServer.close();
    eventPublisher.close();
    executorService.shutdownNow();
  }
}
