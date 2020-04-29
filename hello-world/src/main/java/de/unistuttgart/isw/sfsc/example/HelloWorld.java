package de.unistuttgart.isw.sfsc.example;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.adapter.configuration.AdapterConfiguration;
import de.unistuttgart.isw.sfsc.commonjava.zmq.util.SubscriptionAgent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class HelloWorld {

  public static void main(String[] args) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");

    AdapterConfiguration adapterConfiguration1 = new AdapterConfiguration().setCorePubTcpPort(1251);
    AdapterConfiguration adapterConfiguration2 = new AdapterConfiguration().setCorePubTcpPort(1261);

    ExecutorService executor = Executors.newCachedThreadPool();
    CountDownLatch cdl = new CountDownLatch(2);
    new Thread(() -> {
      try (Adapter adapter1 = Adapter.create(adapterConfiguration1)) {
       SubscriptionAgent.create(adapter1.dataConnection()).addSubscriber(ByteString.copyFromUtf8("adapter1"), new PrintingConsumer("adapter1"), executor);

        while (adapter1.dataConnection().subscriptionTracker().getSubscriptions().size() < 2){
          Thread.sleep(50);
        }

        adapter1.dataConnection().publisher().publish("adapter2", "hello from adapter 1".getBytes());

        Thread.sleep(2000);
        cdl.countDown();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();

    new Thread(() -> {
      try (Adapter adapter2 = Adapter.create(adapterConfiguration2)) {

        SubscriptionAgent.create(adapter2.dataConnection()).addSubscriber(ByteString.copyFromUtf8("adapter2"), new PrintingConsumer("adapter2"), executor);

        while (adapter2.dataConnection().subscriptionTracker().getSubscriptions().size() < 2){
          Thread.sleep(50);
        }

        adapter2.dataConnection().publisher().publish("adapter1", ByteString.copyFromUtf8("hello from adapter 2").toByteArray());

        Thread.sleep(2000);
        cdl.countDown();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();

    try {
      cdl.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    executor.shutdownNow();
  }


  private static class PrintingConsumer implements BiConsumer<ByteString, ByteString> {

    private final String name;

    private PrintingConsumer(String name) {this.name = name;}

    @Override
    public void accept(ByteString topic, ByteString data) {
      System.out.println(name
          + " received message in topic \""
          + topic.toStringUtf8()
          + "\" with content \""
          + data.toStringUtf8()
          + "\""
      );
    }
  }

}
