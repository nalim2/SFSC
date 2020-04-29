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

  BootstrapModule(PubSubConnection pubSubConnection, BootstrapperParameter parameter) {
    publisher = new Publisher(pubSubConnection);

    ByteString topic = parameter.getTopic();
    BootstrapMessage message = BootstrapMessage.newBuilder()
        .setCoreControlPubTcpPort(parameter.getCoreControlPubTcpPort())
        .setCoreControlSubTcpPort(parameter.getCoreControlSubTcpPort())
        .setCoreDataPubTcpPort(parameter.getCoreDataPubTcpPort())
        .setCoreDataSubTcpPort(parameter.getCoreDataSubTcpPort())
        .setCoreControlPubIpcFile(parameter.getCoreControlPubIpcFile())
        .setCoreControlSubIpcFile(parameter.getCoreControlSubIpcFile())
        .setCoreDataPubIpcFile(parameter.getCoreDataPubIpcFile())
        .setCoreDataSubIpcFile(parameter.getCoreDataSubIpcFile())
        .build();
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
