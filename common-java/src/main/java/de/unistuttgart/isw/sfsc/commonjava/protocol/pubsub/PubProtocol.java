package de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub;

import java.util.List;

public enum PubProtocol {
  TOPIC_FRAME(0),
  DATA_FRAME(1);

  private static final int LENGTH = values().length; //cache
  private final int position;

  PubProtocol(int position) {
    this.position = position;
    assert this.position == ordinal();
  }

  public static int frameCount() {
    return LENGTH;
  }

  public static List<byte[]> newMessage(byte[] topic, byte[] data) {
    return List.of(topic, data);
  }

  public static boolean isValid(List<byte[]> message){
    return message.size() == LENGTH;
  }

  public static byte[] getTopic(List<byte[]> message) {
    return message.get(TOPIC_FRAME.position);
  }

  public static byte[] getData(List<byte[]> message) {
    return message.get(DATA_FRAME.position);
  }
}
