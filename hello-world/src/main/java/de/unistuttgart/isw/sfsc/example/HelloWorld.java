package de.unistuttgart.isw.sfsc.example;

import static de.unistuttgart.isw.sfsc.util.Util.dataMessage;
import static de.unistuttgart.isw.sfsc.util.Util.subscriptionMessage;
import static protocol.pubsub.SubProtocol.TYPE_AND_TOPIC_FRAME;

import de.unistuttgart.isw.sfsc.client.adapter.Adapter;
import de.unistuttgart.isw.sfsc.client.adapter.BootstrapConfiguration;
import protocol.pubsub.DataProtocol;
import protocol.pubsub.SubProtocol;

public class HelloWorld {

  public static void main(String[] args) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");

    BootstrapConfiguration bootstrapConfiguration1 = new BootstrapConfiguration("127.0.0.1", 1251);
    BootstrapConfiguration bootstrapConfiguration2 = new BootstrapConfiguration("127.0.0.1", 1261);

    new Thread(() -> {
      try (Adapter adapter1 = Adapter.create(bootstrapConfiguration1)) {

        adapter1.getDataClient().subscriptionManager().outbox().add(subscriptionMessage("topic1"));
        System.out.println("adapter1 sent subscription");

        System.out.println("adapter1 received subscription " + new String(SubProtocol.getTopic(TYPE_AND_TOPIC_FRAME.get(adapter1.getDataClient().subEventInbox().take()))));
        System.out.println("adapter1 received subscription " + new String(SubProtocol.getTopic(TYPE_AND_TOPIC_FRAME.get(adapter1.getDataClient().subEventInbox().take()))));

        adapter1.getDataClient().publisher().outbox().add(dataMessage("topic1", "messageFromAdapter1"));
        System.out.println("adapter1 sent message");

        System.out.println("adapter1 received message " + new String(DataProtocol.PAYLOAD_FRAME.get(adapter1.getDataClient().dataInbox().take())));

        Thread.sleep(2000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();

    new Thread(() -> {
      try (Adapter adapter2 = Adapter.create(bootstrapConfiguration2)) {

        adapter2.getDataClient().subscriptionManager().outbox().add(subscriptionMessage("topic2"));
        System.out.println("adapter2 sent subscription");

        System.out.println("adapter2 received subscription " +  new String(SubProtocol.getTopic(TYPE_AND_TOPIC_FRAME.get(adapter2.getDataClient().subEventInbox().take()))));
        System.out.println("adapter2 received subscription " +  new String(SubProtocol.getTopic(TYPE_AND_TOPIC_FRAME.get(adapter2.getDataClient().subEventInbox().take()))));

        adapter2.getDataClient().publisher().outbox().add(dataMessage("topic2", "messageFromAdapter2"));
        System.out.println("adapter2 sent message");

        System.out.println("adapter2 received message " + new String(DataProtocol.PAYLOAD_FRAME.get(adapter2.getDataClient().dataInbox().take())));

        Thread.sleep(2000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
  }


}
