package de.unistuttgart.isw.sfsc.adapter.control.bootstrapping;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.clientserver.protocol.bootstrap.BootstrapMessage;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Subscriber;
import de.unistuttgart.isw.sfsc.commonjava.util.FutureAdapter;
import de.unistuttgart.isw.sfsc.commonjava.util.scheduling.Scheduler;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Bootstrapper {

  public static BootstrapMessage bootstrap(BootstrapperParameter params, PubSubConnection pubSubConnection, Scheduler scheduler)
      throws InterruptedException, ExecutionException, TimeoutException {

    FutureAdapter<ByteString, BootstrapMessage> futureAdapter = new FutureAdapter<>(
        BootstrapMessage::parseFrom,
        () -> {throw new TimeoutException();}
    );

    try (Subscriber ignored = new Subscriber(pubSubConnection, futureAdapter::handleInput, params.getRemoteTopic(), scheduler)) {
      return futureAdapter.get(params.getTimeoutMs(), TimeUnit.MILLISECONDS);
    }
  }

}
