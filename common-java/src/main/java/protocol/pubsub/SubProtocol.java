package protocol.pubsub;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import protocol.Frame;

public enum SubProtocol implements Frame {
  TYPE_AND_TOPIC_FRAME(0);

  public static final int TYPE_AND_TOPIC_FRAME_SUBSCRIPTION_TYPE_POSITION = 0; //first byte
  private static final int LENGTH = values().length; //cache
  private final int position;

  public static int getLength() {
    return LENGTH;
  }

  public static byte[][] newEmptyMessage() {
    return new byte[LENGTH][];
  }

  SubProtocol(int position) {
    this.position = position;
    assert this.position == ordinal();
  }

  public int getFramePosition() {
    return position;
  }

  public static SubscriptionType getSubscriptionType(byte[] typeAndTopicFrame) {
    return SubscriptionType.ofValue(typeAndTopicFrame[TYPE_AND_TOPIC_FRAME_SUBSCRIPTION_TYPE_POSITION]);
  }

  public static byte[] getTopic(byte[] typeAndTopicFrame) {
    return Arrays.copyOfRange(typeAndTopicFrame, TYPE_AND_TOPIC_FRAME_SUBSCRIPTION_TYPE_POSITION + 1, typeAndTopicFrame.length);
  }

  public static byte[] buildTypeAndTopicFrame(SubscriptionType subscriptionType, byte[] topic) {
    byte[] data = new byte[1 + topic.length];
    data[0] = subscriptionType.getValue();
    System.arraycopy(topic, 0, data, 1, topic.length);
    return data;
  }

  public enum SubscriptionType {
    UNSUBSCRIPTION((byte) 0), SUBSCRIPTION((byte) 1);

    private static final Map<Byte, SubscriptionType> values = Collections.unmodifiableMap(
        Arrays.stream(SubscriptionType.values())
            .collect(Collectors.toMap(SubscriptionType::getValue, type -> type)));

    private final byte value;

    SubscriptionType(byte value) {
      this.value = value;
    }

    public byte getValue() {
      return value;
    }

    public static SubscriptionType ofValue(byte value) {
      SubscriptionType type = values.get(value);
      if (type == null) {
        throw new IllegalArgumentException();
      }
      return type;
    }
  }
}
