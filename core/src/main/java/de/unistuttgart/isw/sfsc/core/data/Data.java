package de.unistuttgart.isw.sfsc.core.data;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubSocketPair;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.forwarder.ForwardingInbox;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Connector;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactorFactory;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.TransportProtocol;
import de.unistuttgart.isw.sfsc.core.CoreParameter;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Data implements NotThrowingAutoCloseable {

  private final PubSubSocketPair frontend;
  private final PubSubSocketPair backend;
  private final CoreParameter parameter;
  private final ForwardingInbox backendDataInbox;
  private final ForwardingInbox backendSubscriptionInbox;
  private final ForwardingInbox frontendDataInbox;
  private final ForwardingInbox frontendSubscriptionInbox;
  private final Reactor reactor;

  Data(CoreParameter parameter) throws ExecutionException, InterruptedException {
    this.parameter = parameter;
    reactor = ReactorFactory.create();
    frontend = PubSubSocketPair.create(reactor);
    backend = PubSubSocketPair.create(reactor);

    backendDataInbox = ForwardingInbox.create(backend.dataInbox());
    backendSubscriptionInbox = ForwardingInbox.create(backend.subscriptionInbox());
    frontendDataInbox = ForwardingInbox.create(frontend.dataInbox());
    frontendSubscriptionInbox = ForwardingInbox.create(frontend.subscriptionInbox());

    backendDataInbox.addListener(frontend.dataOutbox()::add);
    backendSubscriptionInbox.addListener(frontend.subscriptionOutbox()::add);
    frontendDataInbox.addListener(backend.dataOutbox()::add);
    frontendDataInbox.addListener(frontend.dataOutbox()::add);
    frontendSubscriptionInbox.addListener(backend.subscriptionOutbox()::add);
    frontendSubscriptionInbox.addListener(frontend.subscriptionOutbox()::add);

    backendDataInbox.start();
    backendSubscriptionInbox.start();
    frontendDataInbox.start();
    frontendSubscriptionInbox.start();
  }

  public static Data create(CoreParameter parameter) throws ExecutionException, InterruptedException, IOException {
    Data data = new Data(parameter);

    File pub = new File(parameter.getIpcFolderLocation(), parameter.getDataPubIpcFile());
    File sub = new File(parameter.getIpcFolderLocation(), parameter.getDataSubIpcFile());
    pub.createNewFile();
    sub.createNewFile();
    String frontendPubIpcPath = pub.getAbsolutePath();
    String frontendSubIpcPath = sub.getAbsolutePath();
    data.frontend.publisherSocketConnector().bind(TransportProtocol.IPC, frontendPubIpcPath);
    data.frontend.subscriberSocketConnector().bind(TransportProtocol.IPC, frontendSubIpcPath);

    String frontendPubTcpAddress = Connector.createWildcardAddress(parameter.getDataPubTcpPort());
    String frontendSubTcpAddress = Connector.createWildcardAddress(parameter.getDataSubTcpPort());
    data.frontend.publisherSocketConnector().bind(TransportProtocol.TCP, frontendPubTcpAddress);
    data.frontend.subscriberSocketConnector().bind(TransportProtocol.TCP, frontendSubTcpAddress);

    String backendTcpAddress = Connector.createWildcardAddress(parameter.getDataBackendTcpPort());
    data.backend.subscriberSocketConnector().bind(TransportProtocol.TCP, backendTcpAddress);
    return data;
  }

  public void connectBackend(String host, int port) {
    if (!parameter.getBackendHost().equals(host) || parameter.getDataBackendTcpPort() != port) {
      backend.publisherSocketConnector().connect(TransportProtocol.TCP, Connector.createAddress(host, port));
    }
  }

  public void disconnectBackend(String host, int port) {
    if (!parameter.getBackendHost().equals(host) || parameter.getDataBackendTcpPort() != port) {
      backend.publisherSocketConnector().disconnect(TransportProtocol.TCP, Connector.createAddress(host, port));
    }
  }

  @Override
  public void close() {
    backendDataInbox.close();
    backendSubscriptionInbox.close();
    frontendDataInbox.close();
    frontendSubscriptionInbox.close();
    reactor.close();
    frontend.close();
    backend.close();
  }
}
