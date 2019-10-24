package de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub;

import de.unistuttgart.isw.sfsc.commonjava.protocol.Frame;

public enum DataProtocol implements Frame {
  TOPIC_FRAME(0),
  DATA_FRAME(1);

  private static final int LENGTH = values().length; //cache
  private final int position;

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
