package de.unistuttgart.isw.sfsc.core.control.session;

import de.unistuttgart.isw.sfsc.commonjava.heartbeating.HeartbeatModule;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.SchedulerService;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.function.Consumer;

public class SessionModule implements NotThrowingAutoCloseable {

  private final SessionServer sessionServer;
  private final SchedulerService schedulerService;

  SessionModule(SessionParameter parameter, PubSubConnection pubSubConnection, SchedulerService schedulerService,
      Listeners<Consumer<NewSessionEvent>> sessionListeners) {
    sessionServer = new SessionServer(parameter, pubSubConnection, schedulerService, sessionListeners);
    this.schedulerService = schedulerService;
  }

  public static SessionModule create(PubSubConnection pubSubConnection, SessionParameter parameter, Consumer<String> onDead) {
    SchedulerService schedulerService = new SchedulerService(1);
    HeartbeatModule heartbeatModule = HeartbeatModule.create(pubSubConnection, schedulerService, parameter.getHeartbeatParameter());
    Listeners<Consumer<NewSessionEvent>> sessionListeners = new Listeners<>();
    sessionListeners.add(
        (event) -> heartbeatModule.startSession(event.getAdapterId(), event.getHeartbeatAdapterTopic(), onDead));
    return new SessionModule(parameter, pubSubConnection, schedulerService, sessionListeners);
  }

  @Override
  public void close() {
    sessionServer.close();
    schedulerService.close();
  }
}
