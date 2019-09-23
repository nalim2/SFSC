package de.unistuttgart.isw.sfsc.client.adapter;

import de.unistuttgart.isw.sfsc.client.adapter.raw.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.client.adapter.raw.RawAdapter;
import de.unistuttgart.isw.sfsc.client.adapter.raw.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager.InboxManager;
import de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager.TopicListener;
import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.SubscriptionEventProcessor;
import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.SubscriptionTracker;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.OutputPublisher;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactiveinbox.ReactiveInbox;
import java.util.concurrent.ExecutionException;

public class Adapter implements AutoCloseable {

  private final RawAdapter rawAdapter;
  private final InboxManager inboxManager;
  private final ReactiveInbox reactiveDataInbox;
  private final ReactiveInbox reactiveSubInbox;
  private final SubscriptionTracker subscriptionTracker;

  Adapter(RawAdapter rawAdapter) {
    this.rawAdapter = rawAdapter;
    this.inboxManager = new InboxManager(rawAdapter.dataConnection().subscriptionManager());
    this.reactiveDataInbox = ReactiveInbox.create(rawAdapter.dataConnection().dataInbox(), inboxManager);
    this.subscriptionTracker = new SubscriptionTracker();
    this.reactiveSubInbox = ReactiveInbox.create(rawAdapter.dataConnection().subEventInbox(), new SubscriptionEventProcessor(subscriptionTracker));
  }

  public static Adapter create(BootstrapConfiguration bootstrapConfiguration) throws ExecutionException, InterruptedException {
    RawAdapter rawAdapter = RawAdapter.create(bootstrapConfiguration);
    return new Adapter(rawAdapter);
  }

  public RegistryClient registryClient() {
    return rawAdapter.registryClient();
  }

  public void addListener(TopicListener topicListener) {
    inboxManager.addTopic(topicListener);
  }

  public void removeListener(TopicListener topicListener) {
    inboxManager.removeTopic(topicListener);
  }

  public OutputPublisher publisher() {
    return rawAdapter.dataConnection().publisher();
  }

  public SubscriptionTracker subscriptionTracker() {
    return subscriptionTracker;
  }

  @Override
  public void close() {
    rawAdapter.close();
    reactiveDataInbox.close();
    reactiveSubInbox.close();
  }
}
