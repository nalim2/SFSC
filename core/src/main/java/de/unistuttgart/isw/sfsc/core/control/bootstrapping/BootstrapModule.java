package de.unistuttgart.isw.sfsc.core.control.bootstrapping;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.clientserver.protocol.bootstrap.BootstrapMessage;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Publisher;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent.StoreEventType;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BootstrapModule implements NotThrowingAutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(BootstrapModule.class);

  private final Publisher publisher;
  private final Handle handle;

  BootstrapModule(PubSubConnection pubSubConnection, BootstrapperParameter params) {
    publisher = new Publisher(pubSubConnection);

    ByteString topic = params.getTopic();
    int port = params.getSubscriptionPort();
    BootstrapMessage message = BootstrapMessage.newBuilder().setCoreSubscriptionPort(port).build();

    handle = pubSubConnection.subscriptionTracker().addListener(storeEvent -> {
          if (topic.equals(storeEvent.getData()) && storeEvent.getStoreEventType() == StoreEventType.CREATE) {
            logger.info("received new subscription on bootstrap topic, sending bootstrapMessage");
            publisher.publish(topic, message);
          }
        }
    );
  }

  public static BootstrapModule create(PubSubConnection pubSubConnection, BootstrapperParameter params) {
    return new BootstrapModule(pubSubConnection, params);
  }

  @Override
  public void close() {
    handle.close();
  }
}
