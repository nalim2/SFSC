package de.unistuttgart.isw.sfsc.core.control.bootstrapping;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.clientserver.protocol.bootstrap.BootstrapMessage;
import de.unistuttgart.isw.sfsc.commonjava.patterns.pubsub.Publisher;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent.StoreEventType;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BootstrapModule implements NotThrowingAutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(BootstrapModule.class);
  private static final String CORE_BOOTSTRAPPING_TOPIC = "BOOTSTRAP";

  private final ByteString topic = ByteString.copyFromUtf8(CORE_BOOTSTRAPPING_TOPIC);
  private final Publisher publisher;
  private final Handle handle;

  BootstrapModule(PubSubConnection pubSubConnection, Configuration<CoreOption> configuration) {
    int port = Integer.parseInt(configuration.get(CoreOption.CONTROL_SUB_PORT));
    publisher = new Publisher(pubSubConnection);
    handle = pubSubConnection.subscriptionTracker().addListener(storeEvent -> {
          if (topic.equals(storeEvent.getData()) && storeEvent.getStoreEventType() == StoreEventType.CREATE) {
            logger.info("received new subscription on bootstrap topic, sending bootstrapMessage");
            publisher.publish(topic, BootstrapMessage.newBuilder().setCoreSubscriptionPort(port).build());
          }
        }
    );
  }

  public static BootstrapModule create(PubSubConnection pubSubConnection, Configuration<CoreOption> configuration) {
    return new BootstrapModule(pubSubConnection, configuration);
  }

  @Override
  public void close() {
    handle.close();
  }
}
