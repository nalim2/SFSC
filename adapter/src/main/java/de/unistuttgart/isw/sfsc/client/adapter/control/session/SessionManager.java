package de.unistuttgart.isw.sfsc.client.adapter.control.session;

import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import zmq.processors.MessageDistributor.TopicListener;
import zmq.processors.SubscriptionEventProcessor.SubscriptionListener;

public interface SessionManager extends TopicListener, SubscriptionListener, AutoCloseable {

  String TOPIC = "session";

  Future<WelcomeMessage> getWelcomeMessage();

  void awaitSessionReady() throws ExecutionException, InterruptedException;

  @Override
  void close();
}
