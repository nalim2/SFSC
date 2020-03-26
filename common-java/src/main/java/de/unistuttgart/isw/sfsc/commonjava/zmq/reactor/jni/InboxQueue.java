package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.jni;

import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Inbox;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class InboxQueue implements Inbox {
  private final BlockingQueue<List<byte[]>> inboxQueue = new LinkedBlockingQueue<>();

  @Override
  public List<byte[]> take() throws InterruptedException {
   return inboxQueue.take();
  }

  void addInboxMessage(byte[][] data) {
    inboxQueue.offer(Arrays.asList(data));
  }
}
