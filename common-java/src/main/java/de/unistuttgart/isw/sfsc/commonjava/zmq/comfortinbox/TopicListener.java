package de.unistuttgart.isw.sfsc.commonjava.zmq.comfortinbox;

import com.google.protobuf.ByteString;

public interface TopicListener {

  ByteString getTopic();

  boolean test(ByteString topic);

  void processMessage(byte[][] message);
}
