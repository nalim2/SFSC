package de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub;

import java.util.Arrays;
import java.util.List;

public enum SubProtocol {
  TYPE_AND_TOPIC_FRAME(0);

  private static final int LENGTH = values().length; //cache
  private final int position;

  SubProtocol(int position) {
    this.position = position;
    assert this.position == ordinal();
  }

  public static List<byte[]> newMessage(SubscriptionType type, byte[] topic) {
    return List.of(TypeAndTopicFrame.build(type, topic));
  }

  public static boolean isValid(List<byte[]> message) {
    return message.size() == LENGTH && TypeAndTopicFrame.isValid(getTypeAndTopicFrame(message));
  }

  public static SubscriptionType getSubscriptionType(List<byte[]> message) {
    return TypeAndTopicFrame.getSubscriptionType(getTypeAndTopicFrame(message));
  }

  public static byte[] getTopic(List<byte[]> message) {
    return TypeAndTopicFrame.getTopic(getTypeAndTopicFrame(message));
  }

  static byte[] getTypeAndTopicFrame(List<byte[]> message) {
    return message.get(TYPE_AND_TOPIC_FRAME.position);
  }

  static final class TypeAndTopicFrame {

    private static final int TYPE_AND_TOPIC_FRAME_SUBSCRIPTION_TYPE_POSITION = 0; //first byte

    static byte[] build(SubscriptionType subscriptionType, byte[] topic) {
      byte[] data = new byte[1 + topic.length];
      data[0] = SubscriptionType.toByte(subscriptionType);
      System.arraycopy(topic, 0, data, 1, topic.length);
      return data;
    }

    static boolean isValid(byte[] frame) {
      return SubscriptionType.isValid(frame[TYPE_AND_TOPIC_FRAME_SUBSCRIPTION_TYPE_POSITION]);
    }

    static SubscriptionType getSubscriptionType(byte[] frame) {
      return SubscriptionType.get(frame[TYPE_AND_TOPIC_FRAME_SUBSCRIPTION_TYPE_POSITION]);
    }

    static byte[] getTopic(byte[] frame) {
      return Arrays.copyOfRange(frame, TYPE_AND_TOPIC_FRAME_SUBSCRIPTION_TYPE_POSITION + 1, frame.length);
    }

  }

  public enum SubscriptionType {
    UNSUBSCRIPTION((byte) 0),
    SUBSCRIPTION((byte) 1);

    private final byte value;

    SubscriptionType(byte value) {
      this.value = value;
    }

    static byte toByte(SubscriptionType subscriptionType) {
      return subscriptionType.value;
    }

    static boolean isValid(byte subscriptionByte) {
      return subscriptionByte == UNSUBSCRIPTION.value || subscriptionByte == SUBSCRIPTION.value;
    }

    static SubscriptionType get(byte subscriptionByte) {
      return subscriptionByte == SUBSCRIPTION.value ? SUBSCRIPTION : UNSUBSCRIPTION;
    }
  }
}
