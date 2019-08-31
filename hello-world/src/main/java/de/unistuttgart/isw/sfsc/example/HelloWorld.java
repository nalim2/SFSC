package de.unistuttgart.isw.sfsc.example;

import static protocol.pubsub.SubProtocol.TYPE_AND_TOPIC_FRAME;

import de.unistuttgart.isw.sfsc.client.adapter.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.client.adapter.RawAdapter;
import protocol.pubsub.DataProtocol;
import protocol.pubsub.SubProtocol;

public class HelloWorld {

  public static void main(String[] args) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");

    BootstrapConfiguration bootstrapConfiguration1 = new BootstrapConfiguration("127.0.0.1", 1251);
    BootstrapConfiguration bootstrapConfiguration2 = new BootstrapConfiguration("127.0.0.1", 1261);

    new Thread(() -> {
      try (RawAdapter adapter1 = RawAdapter.create(bootstrapConfiguration1)) {

        adapter1.dataClient().subscriptionManager().subscribe("topic1");
        System.out.println("adapter1 sent subscription");

        System.out.println("adapter1 received subscription " + new String(SubProtocol.getTopic(TYPE_AND_TOPIC_FRAME.get(adapter1.dataClient().subEventInbox().take()))));
        System.out.println("adapter1 received subscription " + new String(SubProtocol.getTopic(TYPE_AND_TOPIC_FRAME.get(adapter1.dataClient().subEventInbox().take()))));

        adapter1.dataClient().publisher().publish("topic1", "messageFromAdapter1".getBytes());
        System.out.println("adapter1 sent message");

        System.out.println("adapter1 received message " + new String(DataProtocol.PAYLOAD_FRAME.get(adapter1.dataClient().dataInbox().take())));

        Thread.sleep(2000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();

    new Thread(() -> {
      try (RawAdapter adapter2 = RawAdapter.create(bootstrapConfiguration2)) {

        adapter2.dataClient().subscriptionManager().subscribe("topic2");
        System.out.println("adapter2 sent subscription");

        System.out.println("adapter2 received subscription " +  new String(SubProtocol.getTopic(TYPE_AND_TOPIC_FRAME.get(adapter2.dataClient().subEventInbox().take()))));
        System.out.println("adapter2 received subscription " +  new String(SubProtocol.getTopic(TYPE_AND_TOPIC_FRAME.get(adapter2.dataClient().subEventInbox().take()))));

        adapter2.dataClient().publisher().publish("topic2", "messageFromAdapter2".getBytes());
        System.out.println("adapter2 sent message");

        System.out.println("adapter2 received message " + new String(DataProtocol.PAYLOAD_FRAME.get(adapter2.dataClient().dataInbox().take())));

        Thread.sleep(2000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
  }


}
