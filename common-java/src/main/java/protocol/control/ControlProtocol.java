package protocol.control;

import protocol.Frame;

public enum ControlProtocol implements Frame {
  TOPIC_FRAME(0),
  HEADER_FRAME(1),
  PAYLOAD_FRAME(2);

  private static final int LENGTH = values().length; //cache
  private final int position;

  public static int getLength() {
    return LENGTH;
  }

  public static byte[][] newEmptyMessage() {
    return new byte[LENGTH][];
  }

  ControlProtocol(int position) {
    this.position = position;
    assert this.position == ordinal();
  }

  public int getFramePosition() {
    return position;
  }

}
