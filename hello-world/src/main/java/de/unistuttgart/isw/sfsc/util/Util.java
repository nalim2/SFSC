package de.unistuttgart.isw.sfsc.util;

import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.client.adapter.Adapter;
import java.util.concurrent.ThreadLocalRandom;
import protocol.pubsub.DataProtocol;
import protocol.pubsub.SubProtocol;
import protocol.pubsub.SubProtocol.SubscriptionType;

public class Util {
  public static byte[][] dataMessage(String topic, String data){
    return dataMessage(topic.getBytes(), data.getBytes());
  }

  public static byte[][] dataMessage(byte[] topic, byte[] data){
    byte[][] dataMessage = DataProtocol.newEmptyMessage();
    DataProtocol.TOPIC_FRAME.put(dataMessage, topic);
    DataProtocol.PAYLOAD_FRAME.put(dataMessage, data);
    return dataMessage;
  }

  public static byte[][] dataMessage(byte[] topic, Message data){
    return dataMessage(topic, data.toByteArray());
  }

  public static byte[][] subscriptionMessage(String topic){
    return subscriptionMessage(topic.getBytes());
  }

  public static byte[][] subscriptionMessage(byte[] topic){
    byte[][] subscriptionMessage = SubProtocol.newEmptyMessage();
    SubProtocol.TYPE_AND_TOPIC_FRAME.put(subscriptionMessage, SubProtocol.buildTypeAndTopicFrame(SubscriptionType.SUBSCRIPTION, topic));
    return subscriptionMessage;
  }

  public static byte[] pair(Adapter sender, Adapter receiver, int topicSizeBytes) throws InterruptedException {
    byte[] topic = new byte[topicSizeBytes];
    ThreadLocalRandom.current().nextBytes(topic);
    receiver.getDataClient().subscriptionManager().outbox().add(subscriptionMessage(topic));
    sender.getDataClient().subEventInbox().take();
    return topic;
  }
}
