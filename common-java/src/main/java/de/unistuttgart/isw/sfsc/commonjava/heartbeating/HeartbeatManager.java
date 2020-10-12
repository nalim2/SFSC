package de.unistuttgart.isw.sfsc.commonjava.heartbeating;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.heartbeat.HeartbeatMessage;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Publisher;
import de.unistuttgart.isw.sfsc.commonjava.util.DeadMansSwitch;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import de.unistuttgart.isw.sfsc.framework.types.SfscId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HeartbeatManager {

  private static final Logger logger = LoggerFactory.getLogger(HeartbeatManager.class);
  private final PubSubConnection pubSubConnection;
  private final Scheduler scheduler;
  private final Map<String, DeadMansSwitch> sessionMap = new ConcurrentHashMap<>();
  private final HeartbeatParameter params;

  HeartbeatManager(PubSubConnection pubSubConnection, Scheduler scheduler, HeartbeatParameter params) {
    this.pubSubConnection = pubSubConnection;
    this.scheduler = scheduler;
    this.params = params;
  }

  void startSession(String remoteId, ByteString remoteTopic, Consumer<String> onDeceive) {
    final int expectedHeartbeatRate = params.getHeartbeatDeadlineIncomingMs();
    DeadMansSwitch deadMansSwitch = DeadMansSwitch.create(scheduler, expectedHeartbeatRate);
    Handle heartbeat = startHeartbeat(remoteTopic);
    sessionMap.put(remoteId, deadMansSwitch);
    deadMansSwitch.addOnDeceaseListener(() -> {
      sessionMap.remove(remoteId, deadMansSwitch);
      heartbeat.close();
      deadMansSwitch.close();
      onDeceive.accept(remoteId);
    });
  }

  void accept(ByteString byteString) {
    try {
      HeartbeatMessage heartbeat = HeartbeatMessage.parseFrom(byteString);
      keepAlive(heartbeat.getAdapterId().getId());
    } catch (InvalidProtocolBufferException e) {
      logger.warn("received malformed message", e);
    }
  }

  void keepAlive(String adapterId) {
    Optional.ofNullable(sessionMap.get(adapterId)).ifPresentOrElse(
        DeadMansSwitch::keepAlive,
        () -> logger.warn("Received heartbeat: No such id {}", adapterId)
    );
  }

  Handle startHeartbeat(ByteString remoteTopic) {
    final String heartbeatId = params.getOutgoingId();
    Publisher publisher = new Publisher(pubSubConnection);
    Message message = HeartbeatMessage.newBuilder().setAdapterId(SfscId.newBuilder().setId(heartbeatId).build()).build();
    Future<?> future = scheduler.scheduleAtFixedRate(() ->
        publisher.publish(remoteTopic, message), 0, params.getSendRateMs(), TimeUnit.MILLISECONDS);
    return () -> future.cancel(true);
  }
}
