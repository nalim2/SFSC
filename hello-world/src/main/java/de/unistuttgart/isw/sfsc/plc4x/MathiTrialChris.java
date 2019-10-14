package de.unistuttgart.isw.sfsc.plc4x;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.adapter.base.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.commonjava.util.ConsumerFuture;
import de.unistuttgart.isw.sfsc.example.plc4x.messages.Plc4xMessage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import servicepatterns.api.SfscClient;
import servicepatterns.api.SfscServer;
import servicepatterns.api.SfscServiceApi;
import servicepatterns.api.SfscServiceApiFactory;
import servicepatterns.api.filters.FilterFactory;
import servicepatterns.basepatterns.ackreqrep.AckServerResult;

public class MathiTrialChris {

  private static final BootstrapConfiguration bootstrapConfiguration1 = new BootstrapConfiguration("127.0.0.1", 1251);
  private static final BootstrapConfiguration bootstrapConfiguration2 = new BootstrapConfiguration("127.0.0.1", 1261);
  private static final int adapterCounterDefinition = 10;


  public static void main(String[] args) throws ExecutionException, InterruptedException {

    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");

    List<ByteString> allAvailableIDS = new LinkedList<>();
    Queue<ByteString> idStack = new ConcurrentLinkedQueue<>();

    for (int counterAdapters = 0; counterAdapters < adapterCounterDefinition; counterAdapters++) {

      Adapter adapter1 = Adapter.create(bootstrapConfiguration1);
      Adapter adapter2 = Adapter.create(bootstrapConfiguration2);

      ByteString uuid_1 = ByteString.copyFromUtf8(UUID.randomUUID().toString());
      ByteString uuid_2 = ByteString.copyFromUtf8(UUID.randomUUID().toString());

      allAvailableIDS.add(uuid_1);
      allAvailableIDS.add(uuid_2);

      SfscServer server1 = SfscServiceApiFactory.getSfscServiceApi(adapter1)
          .server("myServer",
              ByteString.copyFromUtf8("plc4xtype"),
              ByteString.copyFromUtf8(UUID.randomUUID().toString()),
              ByteString.copyFromUtf8("plc4xtype"),
              null,
              Map.of("id", uuid_1),
              plc4xServer(),
              1000,
              100,
              3);

      Thread.sleep(10);
      SfscServer server2 = SfscServiceApiFactory.getSfscServiceApi(adapter2)
          .server("myServer",
              ByteString.copyFromUtf8("plc4xtype"),
              ByteString.copyFromUtf8(UUID.randomUUID().toString()),
              ByteString.copyFromUtf8("plc4xtype"),
             null,
              Map.of("id", uuid_2),
              plc4xServer(),
              1000,
              100,
              3);
      Thread.sleep(10);
    }

    System.out.println("servers created");
    Thread.sleep(1000);

    allAvailableIDS.forEach(id -> {
          int max = (int) (Math.random() * 1000000);
          for (int counter = 0; counter < max; counter++) {
            idStack.add(id);
          }
        }
    );

    Adapter clientAdapter = Adapter.create(bootstrapConfiguration2);
    SfscServiceApi clientSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(clientAdapter);

    for (int threadCounter = 0; threadCounter < 50; threadCounter++) {
      new Thread(() -> {
        SfscClient client = clientSfscServiceApi.client();

        ByteString idFromStack;
        while ((idFromStack = idStack.poll()) != null) {
          try {
            Map<String, ByteString> writeServerTags = clientSfscServiceApi.getServices("myServer").stream()
                .filter(new FilterFactory().byteStringEqualsFilter("id", idFromStack))
                .findAny().orElseThrow();
            ConsumerFuture<ByteString, Plc4xMessage> consumerInstance = plc4XConverter();
            client.request(writeServerTags, request().toByteString(), consumerInstance, 1000, ()-> System.out.println("timeout"));
            consumerInstance.get();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

      }).start();
    }

    while (idStack.size() != 0) {
      System.out.println("Stack size at the moment is: " + idStack.size());
      Thread.sleep(1000);
    }
    System.out.println("Stack size at the moment is: " + idStack.size());
  }


  static Message request() {
    return Plc4xMessage.newBuilder()
        .setType(Plc4xMessage.Type.WRITE_REQUEST)
        .setName("w-String")
        .setQuery("")
        .setValue("thisIsMyTestValue")
        .build();
  }

  static ConsumerFuture<ByteString, Plc4xMessage> plc4XConverter() {
    return new ConsumerFuture<>(response -> {
      try {
        return Plc4xMessage.parseFrom(response);
      } catch (InvalidProtocolBufferException e) {
        throw new RuntimeException(e);
      }
    });
  }

  static Function<ByteString, AckServerResult> plc4xServer() {
    return sfscMessage -> ackServerResult(Plc4xMessage.getDefaultInstance().toByteString());
  }

  static AckServerResult ackServerResult(ByteString response){
    return new AckServerResult(response, ()->{}, () -> System.out.println("plc4x server delivery fail"));
  }


}
