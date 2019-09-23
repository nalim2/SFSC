package de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager;

import com.google.protobuf.ByteString;
import java.util.Set;

public interface TopicListener {

  Set<ByteString> getTopics();

  boolean test(ByteString topic);

  void processMessage(byte[][] message);
}
