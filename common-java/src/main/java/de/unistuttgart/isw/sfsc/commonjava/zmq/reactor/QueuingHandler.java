package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor;

import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Inbox;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.zeromq.ZLoop;
import org.zeromq.ZLoop.IZLoopHandler;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Socket;

class QueuingHandler implements IZLoopHandler {

  private final BlockingQueue<List<byte[]>> queue = new LinkedBlockingQueue<>();
  private final int defaultFrameCount;

  QueuingHandler(int defaultFrameCount) {
    this.defaultFrameCount = defaultFrameCount;
  }

  @Override
  public int handle(ZLoop unused1, PollItem item, Object unused2) {
    List<byte[]> data = read(item.getSocket());
    queue.offer(data);
    return 0;
  }

  List<byte[]> read(Socket socket) {
    List<byte[]> frames = new ArrayList<>(defaultFrameCount);
    do {
      frames.add(socket.recv());
    }
    while (socket.hasReceiveMore());
    return frames;
  }

  Inbox getInbox() {
    return queue::take;
  }
}

