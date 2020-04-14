package de.unistuttgart.isw.sfsc.adapter.data;

import de.unistuttgart.isw.sfsc.adapter.AdapterInformation;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnectionImplementation;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubSocketPair;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Connector;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.TransportProtocol;
import java.util.concurrent.ExecutionException;

public class DataClient implements NotThrowingAutoCloseable {

  private final PubSubSocketPair pubSubSocketPair;
  private final PubSubConnectionImplementation pubSubConnection;

  DataClient(PubSubSocketPair pubSubSocketPair, PubSubConnectionImplementation pubSubConnection) {
    this.pubSubSocketPair = pubSubSocketPair;
    this.pubSubConnection = pubSubConnection;
  }

  public static DataClient create(Reactor reactor, AdapterInformation adapterInformation) throws ExecutionException, InterruptedException {
    PubSubSocketPair pubSubSocketPair = PubSubSocketPair.create(reactor);
    PubSubConnectionImplementation pubSubConnection = PubSubConnectionImplementation.create(pubSubSocketPair);
    pubSubConnection.start();
    pubSubSocketPair.subscriberSocketConnector()
        .connect(TransportProtocol.TCP, Connector.createAddress(adapterInformation.getCoreHost(), adapterInformation.getCoreDataPubPort()));
    pubSubSocketPair.publisherSocketConnector()
        .connect(TransportProtocol.TCP, Connector.createAddress(adapterInformation.getCoreHost(), adapterInformation.getCoreDataSubPort()));

    return new DataClient(pubSubSocketPair, pubSubConnection);
  }

  public PubSubConnection pubSubConnection() {
    return pubSubConnection;
  }

  @Override
  public void close() {
    pubSubSocketPair.close();
    pubSubConnection.close();
  }

}
