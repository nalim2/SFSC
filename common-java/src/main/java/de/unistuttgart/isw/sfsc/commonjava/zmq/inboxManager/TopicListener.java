package de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager;

import com.google.protobuf.ByteString;

public interface TopicListener {

  ByteString getTopic();

  boolean test(ByteString topic);

  void processMessage(byte[][] message);
}
