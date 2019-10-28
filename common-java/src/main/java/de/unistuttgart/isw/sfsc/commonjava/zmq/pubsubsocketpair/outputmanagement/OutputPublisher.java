package de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.outputmanagement;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

public interface OutputPublisher {

  void publish(byte[] topic, byte[] data);

  void publish(ByteString topic, byte[] data);

  void publish(String topic, byte[] data);

  void publish(byte[] topic, Message data);

  void publish(ByteString topic, Message data);

  void publish(String topic, Message data);
}
