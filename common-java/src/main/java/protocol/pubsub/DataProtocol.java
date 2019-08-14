package protocol.pubsub;

import protocol.Frame;

public enum DataProtocol implements Frame {
  TOPIC_FRAME(0),
  PAYLOAD_FRAME(1);

  private static final int LENGTH = values().length; //cache
  private final int position;

  public static int getLength() {
    return LENGTH;
  }

  public static byte[][] newEmptyMessage() {
    return new byte[LENGTH][];
  }

  DataProtocol(int position) {
    this.position = position;
    assert this.position == ordinal();
  }

  public int getFramePosition() {
    return position;
  }

}
