package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair;

import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.data.DataMultiplexer;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.data.DataMultiplexingInbox;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription.SubscriptionTracker;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.inputmanagement.subscription.SubscriptionTrackingInbox;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.OutputPublisher;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.OutputPublisherImplementation;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.SubscriptionManager;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement.SubscriptionManagerImplementation;

public class PubSubConnectionImplementation implements PubSubConnection, NotThrowingAutoCloseable {

  private final OutputPublisher outputPublisher;
  private final DataMultiplexingInbox dataMultiplexingInbox;
  private final SubscriptionManager subscriptionManager;
  private final SubscriptionTrackingInbox subscriptionTrackingInbox;

  PubSubConnectionImplementation(OutputPublisher outputPublisher, DataMultiplexingInbox dataMultiplexingInbox, SubscriptionManager subscriptionManager,
      SubscriptionTrackingInbox subscriptionTrackingInbox) {
    this.outputPublisher = outputPublisher;
    this.dataMultiplexingInbox = dataMultiplexingInbox;
    this.subscriptionManager = subscriptionManager;
    this.subscriptionTrackingInbox = subscriptionTrackingInbox;
  }

  public static PubSubConnectionImplementation create(PubSubSocketPair pubSubSocketPair) {
    OutputPublisher outputPublisher = new OutputPublisherImplementation(pubSubSocketPair.dataOutbox());
    DataMultiplexingInbox dataMultiplexingInbox = DataMultiplexingInbox.create(pubSubSocketPair.dataInbox());
    SubscriptionManager subscriptionManager = new SubscriptionManagerImplementation(pubSubSocketPair.subscriptionOutbox());
    SubscriptionTrackingInbox subscriptionTrackingInbox = SubscriptionTrackingInbox.create(pubSubSocketPair.subscriptionInbox());
    return new PubSubConnectionImplementation(outputPublisher, dataMultiplexingInbox, subscriptionManager, subscriptionTrackingInbox);
  }

  public void start(){
    dataMultiplexingInbox.start();
    subscriptionTrackingInbox.start();
  }

  @Override
  public OutputPublisher publisher() {
    return outputPublisher;
  }

  @Override
  public DataMultiplexer dataMultiplexer() {
    return dataMultiplexingInbox;
  }

  @Override
  public SubscriptionManager subscriptionManager() {
    return subscriptionManager;
  }

  @Override
  public SubscriptionTracker subscriptionTracker() {
    return subscriptionTrackingInbox;
  }

  @Override
  public void close() {
    subscriptionTrackingInbox.close();
    dataMultiplexingInbox.close();
  }
}
