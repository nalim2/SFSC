package de.unistuttgart.isw.sfsc.commonjava.zmq.comfortinbox;

public interface TopicListener {

  String getTopic();

  boolean test(String topic);

  void processMessage(byte[][] message);
}
