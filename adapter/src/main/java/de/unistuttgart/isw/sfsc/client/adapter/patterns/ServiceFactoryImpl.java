package de.unistuttgart.isw.sfsc.client.adapter.patterns;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.client.adapter.raw.RawAdapter;
import de.unistuttgart.isw.sfsc.commonjava.registry.TimeoutRegistry;
import de.unistuttgart.isw.sfsc.commonjava.zmq.comfortinbox.ComfortInbox;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactiveinbox.ReactiveInbox;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.Service;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub.Publisher;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub.PublisherFactory;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub.Subscriber;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub.SubscriberFactory;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.reqrep.Client;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.reqrep.ClientFactory;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.reqrep.Server;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.reqrep.ServerFactory;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.tags.TagCompleter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

public class ServiceFactoryImpl implements ServiceFactory, AutoCloseable {

  private final TimeoutRegistry<Integer, Consumer<SfscMessage>> timeoutRegistry = new TimeoutRegistry<>();
  private final ReactiveInbox reactiveInbox;
  private final RawAdapter rawAdapter;
  private final PublisherFactory publisherFactory;
  private final SubscriberFactory subscriberFactory;
  private final ServerFactory serverFactory;
  private final ClientFactory clientFactory;


  public ServiceFactoryImpl(RawAdapter rawAdapter) {
    this.rawAdapter = rawAdapter;
    ComfortInbox comfortInbox = new ComfortInbox(rawAdapter.dataConnection().subscriptionManager());
    reactiveInbox = ReactiveInbox.create(rawAdapter.dataConnection().dataInbox(), comfortInbox);
    TagCompleter tagCompleter = new TagCompleter(rawAdapter.adapterId(), rawAdapter.coreId());
    publisherFactory = new PublisherFactory(tagCompleter, rawAdapter.dataConnection().publisher(), rawAdapter.registryClient());
    subscriberFactory = new SubscriberFactory(tagCompleter, rawAdapter.registryClient(), comfortInbox);
    serverFactory = new ServerFactory(tagCompleter, rawAdapter.dataConnection().publisher(), subscriberFactory);
    clientFactory = new ClientFactory(tagCompleter, rawAdapter.dataConnection().publisher(), rawAdapter.registryClient(), timeoutRegistry, subscriberFactory);
  }

  @Override
  public Future<Set<Map<String, ByteString>>> getServices() {
    return rawAdapter.registryClient().getServices();
  }

  @Override
  public void register(Service service) {
    rawAdapter.registryClient().addService(service.getTags());
  }

  @Override
  public Publisher publisher(Map<String, ByteString> tags) {
    return publisherFactory.publisher(tags);
  }

  @Override
  public Subscriber subscriber(Map<String, ByteString> publisherTags, Consumer<SfscMessage> consumer, Executor executor) {
    return subscriberFactory.subscriber(publisherTags, consumer, executor);
  }

  @Override
  public Server server(Map<String, ByteString> tags, Function<SfscMessage, byte[]> server, Executor executor) {
    return serverFactory.server(tags, server, executor);
  }

  @Override
  public Client client(Map<String, ByteString> serverTags, Executor executor) {
    return clientFactory.client(serverTags, executor);
  }

  public void close(){
    reactiveInbox.close();
    rawAdapter.close();
    timeoutRegistry.close(); //todo also unregister all services on close
  }
}
