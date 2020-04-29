package de.unistuttgart.isw.sfsc.core.control.session;

import de.unistuttgart.isw.sfsc.commonjava.heartbeating.HeartbeatModule;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class SessionModule implements NotThrowingAutoCloseable {

  private final SessionServer sessionServer;
  private final ScheduledExecutorService executorService;

  SessionModule(SessionParameter parameter, PubSubConnection pubSubConnection, ScheduledExecutorService executorService,
      Listeners<Consumer<NewSessionEvent>> sessionListeners) {
    sessionServer = new SessionServer(parameter, pubSubConnection, executorService, sessionListeners);
    this.executorService = executorService;
  }

  public static SessionModule create(PubSubConnection pubSubConnection, SessionParameter parameter, Consumer<String> onDead) {
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    parameter.getHeartbeatParameter();
    HeartbeatModule heartbeatModule = HeartbeatModule.create(pubSubConnection, executorService, parameter.getHeartbeatParameter());
    Listeners<Consumer<NewSessionEvent>> sessionListeners = new Listeners<>();
    sessionListeners.add(
        (event) -> heartbeatModule.startSession(event.getAdapterId(), event.getHeartbeatAdapterTopic(), onDead));
    return new SessionModule(parameter, pubSubConnection, executorService, sessionListeners);
  }

  @Override
  public void close() {
    sessionServer.close();
    executorService.shutdownNow();
  }
}
