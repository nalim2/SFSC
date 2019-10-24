package de.unistuttgart.isw.sfsc.core.control.session;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionModule implements NotThrowingAutoCloseable {

  private static final String SERVER_TOPIC = "SESSION_SERVER";
  private static final int SESSION_THREAD_NUMBER = 1;

  private static final Logger logger = LoggerFactory.getLogger(SessionServer.class);

  private final ExecutorService executorService = Executors.unconfigurableExecutorService(
      Executors.newFixedThreadPool(SESSION_THREAD_NUMBER, new ExceptionLoggingThreadFactory(getClass().getName(), logger)));
  private final ByteString serverTopic = ByteString.copyFromUtf8(SERVER_TOPIC);
  private final SessionServer sessionServer;

  SessionModule(PubSubConnection pubSubConnection, Configuration<CoreOption> configuration, String coreId) {
    sessionServer = new SessionServer(configuration, pubSubConnection, serverTopic, executorService, coreId);
  }

  public static SessionModule create(PubSubConnection pubSubConnection, Configuration<CoreOption> configuration, String coreId) {
    return new SessionModule(pubSubConnection, configuration, coreId);
  }

  @Override
  public void close() {
    sessionServer.close();
  }
}
