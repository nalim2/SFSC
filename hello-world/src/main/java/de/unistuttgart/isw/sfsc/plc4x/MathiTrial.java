package de.unistuttgart.isw.sfsc.plc4x;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.adapter.base.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.commonjava.util.ConsumerFuture;
import de.unistuttgart.isw.sfsc.example.plc4x.messages.Plc4xMessage;
import de.unistuttgart.isw.sfsc.patterns.tags.RegexDefinition;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import servicepatterns.api.SfscClient;
import servicepatterns.api.SfscServer;
import servicepatterns.api.SfscServiceApi;
import servicepatterns.api.SfscServiceApiFactory;
import servicepatterns.api.filters.FilterFactory;
import servicepatterns.basepatterns.ackreqrep.AckServerResult;

public class MathiTrial {

  private static final BootstrapConfiguration bootstrapConfiguration1 = new BootstrapConfiguration("127.0.0.1", 1251);
  private static final BootstrapConfiguration bootstrapConfiguration2 = new BootstrapConfiguration("127.0.0.1", 1261);
  private static final List<Adapter> allAdapterApis = new LinkedList<>();
  private static final Stack<ByteString> idStack = new Stack<>();

  private static final int adapterCounterDefinition = 100;

  public static void main(String[] args) throws ExecutionException, InterruptedException, PlcConnectionException {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");

    ByteString uuid = ByteString.copyFromUtf8(UUID.randomUUID().toString());

    Adapter serverAdapter = Adapter.create(bootstrapConfiguration1);

    Adapter clientAdapter = Adapter.create(bootstrapConfiguration2);
    SfscServiceApi clientServiceApi = SfscServiceApiFactory.getSfscServiceApi(clientAdapter);

    List<ByteString> allAvailableIDS = new LinkedList<>();

    for (int counterAdapters = 0; counterAdapters < adapterCounterDefinition; counterAdapters++) {

      Adapter adapter1 = Adapter.create(bootstrapConfiguration1);
      Adapter adapter2 = Adapter.create(bootstrapConfiguration2);
      ByteString uuid_1 = ByteString.copyFromUtf8(UUID.randomUUID().toString());
      allAvailableIDS.add(uuid_1);
      ByteString uuid_2 = ByteString.copyFromUtf8(UUID.randomUUID().toString());
      allAvailableIDS.add(uuid_2);
      allAdapterApis.add(adapter1);
      allAdapterApis.add(adapter2);

      SfscServer server = SfscServiceApiFactory.getSfscServiceApi(adapter1)
          .server(
              "myServer",
              ByteString.copyFromUtf8("plc4xtype"),
              ByteString.copyFromUtf8(UUID.randomUUID().toString()),
              ByteString.copyFromUtf8("plc4xtype"),
              RegexDefinition.newBuilder()
                  .addRegexes(RegexDefinition.VarRegex.newBuilder()
                      .setVarName("name")
                      .setStringRegex(RegexDefinition.VarRegex.StringRegex.newBuilder().setRegex("w-String").build())
                      .build())
                  .addRegexes(RegexDefinition.VarRegex.newBuilder()
                      .setVarName("value")
                      .setStringRegex(RegexDefinition.VarRegex.StringRegex.newBuilder().setRegex(".*").build())
                      .build())
                  .build(),
              Map.of("id", uuid_1),
              plc4xserver(adapter1, uuid_1),
              1000,
              100,
              3);
      Thread.sleep(1000);
      SfscServer server2 = SfscServiceApiFactory.getSfscServiceApi(adapter2).server(
          "myServer",
          ByteString.copyFromUtf8("plc4xtype"),
          ByteString.copyFromUtf8(UUID.randomUUID().toString()),
          ByteString.copyFromUtf8("plc4xtype"),
          RegexDefinition.newBuilder()
              .addRegexes(RegexDefinition.VarRegex.newBuilder()
                  .setVarName("name")
                  .setStringRegex(RegexDefinition.VarRegex.StringRegex.newBuilder().setRegex("w-String").build())
                  .build())
              .addRegexes(RegexDefinition.VarRegex.newBuilder()
                  .setVarName("value")
                  .setStringRegex(RegexDefinition.VarRegex.StringRegex.newBuilder().setRegex(".*").build())
                  .build())
              .build(),
          Map.of("id", uuid_2),
          plc4xserver(adapter2, uuid_2),
          1000,
          100,
          3);
      Thread.sleep(1000);

    }

    Thread.sleep(1000);

    allAvailableIDS.forEach(bytes -> {
      int max = (int) (Math.random() * 1000000);
      for (int counter = 0; counter < max; counter++) {
        idStack.push(bytes);
      }
    });

    for (int threadCounter = 0; threadCounter < 50; threadCounter++) {
      new Thread(() -> {
          SfscServiceApi serviceApi = SfscServiceApiFactory.getSfscServiceApi(serverAdapter);

        SfscClient client = clientServiceApi.client();
        while (idStack.size() != 0) {

          ByteString idFromStack = idStack.pop(); //not thread safe
          try {
            Map<String, ByteString> writeServerTags = clientServiceApi.getServices("myServer").stream()
                .filter(new FilterFactory().byteStringEqualsFilter("id", idFromStack)).findAny().orElseThrow();
            ConsumerFuture<ByteString, Plc4xMessage> consumerInstance = DummyFunction();
            client.request(writeServerTags, DummyRequest().toByteString(), consumerInstance, 1000, ()-> System.out.println("timeout"));
            consumerInstance.get();
          } catch (Exception e) {
            e.printStackTrace();
          }


        }

      }).start();
    }

    while (idStack.size() != 0) {
      System.out.println("Stack size at the Moment is: " + idStack.size());
      Thread.sleep(1000);
    }
  }


  static Message DummyRequest() {
    return Plc4xMessage.newBuilder()
        .setType(Plc4xMessage.Type.WRITE_REQUEST)
        .setName("w-String")
        .setQuery("")
        .setValue("thisIsMyTestValue")
        .build();
  }


  static ConsumerFuture<ByteString, Plc4xMessage> DummyFunction() {
    return new ConsumerFuture<>(response -> {
      try {
        return Plc4xMessage.parseFrom(response);
        //System.out.println("Received write response:  \n" + plc4xMessage);
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }
    );
  }


  static Function<ByteString, AckServerResult> plc4xserver(Adapter instance, ByteString uuid) {
    return sfscMessage -> {
      if (!idStack.contains(uuid)) {
        new Thread(() -> {
          try {
            Thread.sleep(2000);
            instance.close();
            //allAdapterApis.remove(instance);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        });
      }
      //System.out.println("Got Something ;)");
      return ackServerResult(Plc4xMessage.getDefaultInstance().toByteString());
    };
  }

  static AckServerResult ackServerResult(ByteString response) {
    return new AckServerResult(response, () -> {}, () -> System.out.println("plc4x server delivery fail"));
  }

}
