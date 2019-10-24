package de.unistuttgart.isw.sfsc.adapter.control.bootstrapping;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.clientserver.protocol.bootstrap.BootstrapMessage;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Subscriber;
import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.util.FutureAdapter;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Bootstrapper implements NotThrowingAutoCloseable {

  private static final String CORE_BOOTSTRAPPING_TOPIC = "BOOTSTRAP";
  private static final int BOOTSTRAP_TIMEOUT_MS = 1000;

  private static final Logger logger = LoggerFactory.getLogger(Bootstrapper.class);
  private final ByteString topic = ByteString.copyFromUtf8(CORE_BOOTSTRAPPING_TOPIC);
  private final ExecutorService executorService = Executors.newSingleThreadExecutor(new ExceptionLoggingThreadFactory(getClass().getName(), logger));

  private final FutureAdapter<ByteString, BootstrapMessage> futureAdapter;
  private final Subscriber subscriber;

  Bootstrapper(PubSubConnection pubSubConnection) {
    futureAdapter = new FutureAdapter<>(BootstrapMessage::parseFrom, () -> {throw new TimeoutException();});
    subscriber = new Subscriber(pubSubConnection, futureAdapter::handleInput, topic, executorService);
  }

  Future<BootstrapMessage> getBootstrapMessage() {
    return futureAdapter;
  }

  public static BootstrapMessage getBootstrapMessage(PubSubConnection pubSubConnection)
      throws InterruptedException, ExecutionException, TimeoutException {
    try (Bootstrapper bootstrapper = new Bootstrapper(pubSubConnection)) {
      return bootstrapper.getBootstrapMessage().get(BOOTSTRAP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }
  }

  @Override
  public void close() {
    subscriber.close();
    executorService.shutdownNow();
  }

}
