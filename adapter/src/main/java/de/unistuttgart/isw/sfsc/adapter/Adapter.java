package de.unistuttgart.isw.sfsc.adapter;

import de.unistuttgart.isw.sfsc.adapter.base.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.adapter.base.RawAdapter;
import de.unistuttgart.isw.sfsc.adapter.base.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager.InboxTopicManager;
import de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager.InboxTopicManagerImpl;
import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.SubscriptionEventProcessor;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactiveinbox.ReactiveInbox;
import de.unistuttgart.isw.sfsc.commonjava.zmq.subscriptiontracker.SubscriptionTracker;
import de.unistuttgart.isw.sfsc.commonjava.zmq.subscriptiontracker.SubscriptionTrackerImpl;
import java.util.concurrent.ExecutionException;

public class Adapter implements AutoCloseable {

  private final RawAdapter rawAdapter;
  private final InboxTopicManagerImpl inboxTopicManager;
  private final ReactiveInbox reactiveDataInbox;
  private final ReactiveInbox reactiveSubInbox;
  private final SubscriptionTrackerImpl subscriptionTracker;
  private final OutputPublisher publisher;

  Adapter(RawAdapter rawAdapter) {
    this.rawAdapter = rawAdapter;
    this.inboxTopicManager = new InboxTopicManagerImpl(rawAdapter.dataConnection().subscriptionManager());
    this.reactiveDataInbox = ReactiveInbox.create(rawAdapter.dataConnection().dataInbox(), inboxTopicManager);
    this.subscriptionTracker = new SubscriptionTrackerImpl();
    this.reactiveSubInbox = ReactiveInbox.create(rawAdapter.dataConnection().subEventInbox(), new SubscriptionEventProcessor(subscriptionTracker));
    this.publisher = rawAdapter.dataConnection().publisher();
  }

  public static Adapter create(BootstrapConfiguration bootstrapConfiguration) throws ExecutionException, InterruptedException {
    RawAdapter rawAdapter = RawAdapter.create(bootstrapConfiguration);
    return new Adapter(rawAdapter);
  }

  public RegistryClient registryClient() {
    return rawAdapter.registryClient();
  }

  public InboxTopicManager inboxTopicManager(){
    return inboxTopicManager;
  }

  public OutputPublisher publisher() {
    return publisher;
  }

  public SubscriptionTracker subscriptionTracker() {
    return subscriptionTracker;
  }

  public String coreId() {
    return rawAdapter.coreId();
  }

  public String adapterId() {
    return rawAdapter.adapterId();
  }

  @Override
  public void close() {
    rawAdapter.close();
    reactiveDataInbox.close();
    reactiveSubInbox.close();
  }
}
