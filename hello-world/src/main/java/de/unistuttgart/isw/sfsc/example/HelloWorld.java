package de.unistuttgart.isw.sfsc.example;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.adapter.base.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol;
import de.unistuttgart.isw.sfsc.commonjava.zmq.inboxManager.TopicListener;
import java.util.Set;

public class HelloWorld {

  public static void main(String[] args) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");

    BootstrapConfiguration bootstrapConfiguration1 = new BootstrapConfiguration("127.0.0.1", 1251);
    BootstrapConfiguration bootstrapConfiguration2 = new BootstrapConfiguration("127.0.0.1", 1261);

    new Thread(() -> {
      try (Adapter adapter1 = Adapter.create(bootstrapConfiguration1)) {

        adapter1.inboxTopicManager().addTopicListener(new SimpleTopicListener ("adapter1"));

        while (adapter1.subscriptionTracker().getSubscriptions().size() < 2){
          Thread.sleep(50);
        };

        adapter1.publisher().publish("adapter2", ByteString.copyFromUtf8("hello from adapter 1").toByteArray());

        Thread.sleep(2000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();

    new Thread(() -> {
      try (Adapter adapter2 = Adapter.create(bootstrapConfiguration2)) {

        adapter2.inboxTopicManager().addTopicListener(new SimpleTopicListener ("adapter2"));
        while (adapter2.subscriptionTracker().getSubscriptions().size() < 2){
          Thread.sleep(50);
        };

        adapter2.publisher().publish("adapter1", ByteString.copyFromUtf8("hello from adapter 2").toByteArray());
        Thread.sleep(2000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
  }


  private static class SimpleTopicListener implements TopicListener{

    private final ByteString topic ;
    private final String name;

    SimpleTopicListener(String name) {
      this.name = name;
      topic = ByteString.copyFromUtf8(name);
    }

    @Override
    public Set<ByteString> getTopics() {
      return Set.of(topic);
    }

    @Override
    public boolean test(ByteString topic) {
      return this.topic.equals(topic);
    }

    @Override
    public void processMessage(byte[][] message) {
      System.out.println(name
          + " received message in topic \""
          + ByteString.copyFrom(DataProtocol.TOPIC_FRAME.get(message)).toStringUtf8()
          + "\" with content \""
          + ByteString.copyFrom(DataProtocol.PAYLOAD_FRAME.get(message)).toStringUtf8()
          + "\""
      );
    }
  }

}
