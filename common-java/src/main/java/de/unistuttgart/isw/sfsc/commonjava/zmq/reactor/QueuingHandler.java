package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor;

import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Inbox;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZLoop;
import org.zeromq.ZLoop.IZLoopHandler;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Socket;

class QueuingHandler implements IZLoopHandler {

  private static final Logger logger = LoggerFactory.getLogger(QueuingHandler.class);
  private final BlockingQueue<byte[][]> queue = new LinkedTransferQueue<>();
  private final int frameNumber;

  QueuingHandler(int frameNumber) {
    this.frameNumber = frameNumber;
  }

  static <InboxT extends Enum<InboxT>> QueuingHandler forProtocol(Class<InboxT> inboxProtocol) {
    return new QueuingHandler(inboxProtocol.getEnumConstants().length);
  }

  @Override
  public int handle(ZLoop unused1, PollItem item, Object unused2) {
    byte[][] data = read(item.getSocket());
    queue.offer(data);
    return 0;
  }

  byte[][] read(Socket socket) {
    byte[][] bytes = new byte[frameNumber][];
    bytes[0] = socket.recv();
    for (int i = 1; i < frameNumber; i++) {
      if (socket.hasReceiveMore()) {
        bytes[i] = socket.recv();
      } else {
        logger.warn("Received too less frames");
      }
    }
    if (socket.hasReceiveMore()) {
      do {
        socket.recv();
      }
      while (socket.hasReceiveMore());
      logger.warn("Received too many frames");
    }
    return bytes;
  }

  Inbox getInbox() {
    return queue::take;
  }
}

