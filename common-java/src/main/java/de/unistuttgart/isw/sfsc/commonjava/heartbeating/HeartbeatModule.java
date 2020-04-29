package de.unistuttgart.isw.sfsc.commonjava.heartbeating;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Subscriber;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class HeartbeatModule implements NotThrowingAutoCloseable {

  private final HeartbeatManager heartbeatManager;
  private final Subscriber subscriber;

  HeartbeatModule(HeartbeatManager heartbeatManager, Subscriber subscriber) {
    this.heartbeatManager = heartbeatManager;
    this.subscriber = subscriber;
  }

  public static HeartbeatModule create(PubSubConnection pubSubConnection, ScheduledExecutorService scheduledExecutorService,
      HeartbeatParameter heartbeatParameter) {
    HeartbeatManager heartbeatManager = new HeartbeatManager(pubSubConnection, scheduledExecutorService, heartbeatParameter);
    Subscriber subscriber = new Subscriber(pubSubConnection, heartbeatManager::accept, heartbeatParameter.getExpectedIncomingTopic(),
        scheduledExecutorService);
    return new HeartbeatModule(heartbeatManager, subscriber);
  }

  public void startSession(String remoteId, ByteString remoteTopic, Consumer<String> onDeceive) {
    heartbeatManager.startSession(remoteId, remoteTopic, onDeceive);
  }

  @Override
  public void close() {
    subscriber.close();
  }
}
