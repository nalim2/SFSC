package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair;

import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.data.DataMultiplexer;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription.SubscriptionTracker;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.OutputPublisher;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.SubscriptionManager;

public interface PubSubConnection {

  OutputPublisher publisher();

  DataMultiplexer dataMultiplexer();

  SubscriptionManager subscriptionManager();

  SubscriptionTracker subscriptionTracker();

}
