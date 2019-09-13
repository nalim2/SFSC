package de.unistuttgart.isw.sfsc.client.adapter.raw.control.session;

import de.unistuttgart.isw.sfsc.commonjava.zmq.comfortinbox.TopicListener;
import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.SubscriptionEventProcessor.SubscriptionListener;
import de.unistuttgart.isw.sfsc.protocol.session.WelcomeMessage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface SessionManager extends TopicListener, SubscriptionListener, AutoCloseable {

  String TOPIC = "session";

  Future<WelcomeMessage> getWelcomeMessage();

  void awaitSessionReady() throws ExecutionException, InterruptedException;

  @Override
  void close();
}
