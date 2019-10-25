package de.unistuttgart.isw.sfsc.adapter.control.session;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Welcome;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleClient;
import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SessionManager implements Session {

  private static final String CORE_SESSION_TOPIC = "SESSION_SERVER";
  private static final int SESSION_THREAD_NUMBER = 1;
  private static final int TIMEOUT_MS = 1000;

  private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

  private final ScheduledExecutorService sessionExecutor = Executors.unconfigurableScheduledExecutorService(
      Executors.newScheduledThreadPool(SESSION_THREAD_NUMBER, new ExceptionLoggingThreadFactory(getClass().getName(), logger)));
  private final String adapterName = UUID.randomUUID().toString();
  private final ByteString serverTopic = ByteString.copyFromUtf8(CORE_SESSION_TOPIC);
  private final ByteString clientTopic = ByteString.copyFromUtf8("SESSION_CLIENT_" + adapterName);
  private final SimpleClient simpleClient;
  private final SessionClient sessionClient;
  private final WelcomeInformation welcomeInformation;

  SessionManager(PubSubConnection pubSubConnection) throws InterruptedException, ExecutionException, TimeoutException {
    try {
      simpleClient = new SimpleClient(pubSubConnection, clientTopic, sessionExecutor);
      sessionClient = new SessionClient(simpleClient, serverTopic, TIMEOUT_MS);
      pubSubConnection.subscriptionTracker().addSubscriptionListener(serverTopic::equals, () -> null).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
      Welcome welcome = sessionClient.sendHello(adapterName).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
      welcomeInformation = new WelcomeInformation(welcome.getCoreId(), adapterName, welcome.getDataPubPort(), welcome.getDataSubPort());
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      close();
      throw e;
    }
  }

  public static SessionManager create(PubSubConnection pubSubConnection) throws InterruptedException, ExecutionException, TimeoutException {
    return new SessionManager(pubSubConnection);
  }

  @Override
  public WelcomeInformation getWelcomeInformation() {
    return welcomeInformation;
  }

  @Override
  public void close() {
    Optional.ofNullable(simpleClient).ifPresent(SimpleClient::close);
    sessionExecutor.shutdownNow();
  }
}
