package de.unistuttgart.isw.sfsc.adapter.control.handshake;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Hello;
import de.unistuttgart.isw.sfsc.clientserver.protocol.session.handshake.Welcome;
import de.unistuttgart.isw.sfsc.commonjava.patterns.simplereqrep.SimpleClient;
import de.unistuttgart.isw.sfsc.commonjava.util.FutureAdapter;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Handshaker {

  public static Welcome handshake(HandshakerParameter parameter, PubSubConnection pubSubConnection, Executor executor, Hello handshakeMessage)
      throws InterruptedException, ExecutionException, TimeoutException {

    FutureAdapter<ByteString, Welcome> future = new FutureAdapter<>(
        Welcome::parseFrom,
        () -> {throw new TimeoutException();}
    );

    try (SimpleClient simpleClient = new SimpleClient(pubSubConnection, parameter.getSessionLocalTopic(), executor)) {
      pubSubConnection.subscriptionTracker().addOneShotSubscriptionListener(parameter.getSessionRemoteTopic(), () -> {})
          .get(parameter.getTimeoutMs(), TimeUnit.MILLISECONDS);
      simpleClient.send(parameter.getSessionRemoteTopic(), handshakeMessage.toByteString(), future::handleInput, parameter.getTimeoutMs(),
          future::handleError);
      return future.get(parameter.getTimeoutMs(), TimeUnit.MILLISECONDS);
    }

  }


}
