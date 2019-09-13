package de.unistuttgart.isw.sfsc.example;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol.TYPE_AND_TOPIC_FRAME;

import de.unistuttgart.isw.sfsc.client.adapter.raw.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.client.adapter.raw.RawAdapter;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol;

public class HelloWorld {

  public static void main(String[] args) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");

    BootstrapConfiguration bootstrapConfiguration1 = new BootstrapConfiguration("127.0.0.1", 1251);
    BootstrapConfiguration bootstrapConfiguration2 = new BootstrapConfiguration("127.0.0.1", 1261);

    new Thread(() -> {
      try (RawAdapter adapter1 = RawAdapter.create(bootstrapConfiguration1)) {

        adapter1.dataConnection().subscriptionManager().subscribe("topic1");
        System.out.println("adapter1 sent subscription");

        System.out.println("adapter1 received subscription " + SubProtocol.getTopic(TYPE_AND_TOPIC_FRAME.get(adapter1.dataConnection().subEventInbox().take())));
        System.out.println("adapter1 received subscription " + SubProtocol.getTopic(TYPE_AND_TOPIC_FRAME.get(adapter1.dataConnection().subEventInbox().take())));

        adapter1.dataConnection().publisher().publish("topic1", "messageFromAdapter1".getBytes());
        System.out.println("adapter1 sent message");

        System.out.println("adapter1 received message " + new String(DataProtocol.PAYLOAD_FRAME.get(adapter1.dataConnection().dataInbox().take())));

        Thread.sleep(2000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();

    new Thread(() -> {
      try (RawAdapter adapter2 = RawAdapter.create(bootstrapConfiguration2)) {

        adapter2.dataConnection().subscriptionManager().subscribe("topic2");
        System.out.println("adapter2 sent subscription");

        System.out.println("adapter2 received subscription " +  SubProtocol.getTopic(TYPE_AND_TOPIC_FRAME.get(adapter2.dataConnection().subEventInbox().take())));
        System.out.println("adapter2 received subscription " +  SubProtocol.getTopic(TYPE_AND_TOPIC_FRAME.get(adapter2.dataConnection().subEventInbox().take())));

        adapter2.dataConnection().publisher().publish("topic2", "messageFromAdapter2".getBytes());
        System.out.println("adapter2 sent message");

        System.out.println("adapter2 received message " + new String(DataProtocol.PAYLOAD_FRAME.get(adapter2.dataConnection().dataInbox().take())));

        Thread.sleep(2000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
  }


}
