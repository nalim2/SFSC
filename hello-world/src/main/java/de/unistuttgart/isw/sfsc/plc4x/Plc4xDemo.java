package de.unistuttgart.isw.sfsc.plc4x;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.adapter.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.example.plc4x.messages.Plc4xMessage;
import de.unistuttgart.isw.sfsc.example.plc4x.messages.Plc4xMessage.Type;
import de.unistuttgart.isw.sfsc.framework.descriptor.RegexDefinition;
import de.unistuttgart.isw.sfsc.framework.descriptor.RegexDefinition.VarRegex;
import de.unistuttgart.isw.sfsc.framework.descriptor.RegexDefinition.VarRegex.StringRegex;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import servicepatterns.api.SfscClient;
import servicepatterns.api.SfscPublisher;
import servicepatterns.api.SfscServer;
import servicepatterns.api.SfscServiceApi;
import servicepatterns.api.SfscServiceApiFactory;
import servicepatterns.api.SfscSubscriber;
import servicepatterns.api.filters.FilterFactory;
import servicepatterns.basepatterns.ackreqrep.AckServerResult;

public class Plc4xDemo {

  private static final String SERVER_URL = "opcua:tcp://opcua.demo-this.com:51210/UA/SampleServer";

  private static final String BOOL_IDENTIFIER = "ns=2;i=10844";
  private static final String BYTE_STRING_IDENTIFIER = "ns=2;i=10858";
  private static final String BYTE_IDENTIFIER = "ns=2;i=10846";
  private static final String DOUBLE_IDENTIFIER = "ns=2;i=10854";
  private static final String FLOAT_IDENTIFIER = "ns=2;i=10853";
  private static final String INT16_IDENTIFIER = "ns=2;i=10847";
  private static final String INT32_IDENTIFIER = "ns=2;i=10849";
  private static final String INT64_IDENTIFIER = "ns=2;i=10851";
  private static final String INTEGER_IDENTIFIER = "ns=2;i=10869";
  private static final String SBYTE_IDENTIFIER = "ns=2;i=10845";
  private static final String STRING_IDENTIFIER = "ns=2;i=10855";
  private static final String UINT16_IDENTIFIER = "ns=2;i=10848";
  private static final String UINT32_IDENTIFIER = "ns=2;i=10850";
  private static final String UINT64_IDENTIFIER = "ns=2;i=10852";
  private static final String UINTEGER_IDENTIFIER = "ns=2;i=10870";

  private static final Map<String, String> topics = Map.ofEntries(
      Map.entry("Bool", BOOL_IDENTIFIER),
      Map.entry("ByteString", BYTE_STRING_IDENTIFIER),
      Map.entry("Byte", BYTE_IDENTIFIER),
      Map.entry("Double", DOUBLE_IDENTIFIER),
      Map.entry("Float", FLOAT_IDENTIFIER),
      Map.entry("Int16", INT16_IDENTIFIER),
      Map.entry("Int32", INT32_IDENTIFIER),
      Map.entry("Int64", INT64_IDENTIFIER),
      Map.entry("Integer", INTEGER_IDENTIFIER),
      Map.entry("SByte", SBYTE_IDENTIFIER),
      Map.entry("String", STRING_IDENTIFIER),
      Map.entry("UInt16", UINT16_IDENTIFIER),
      Map.entry("UInt32", UINT32_IDENTIFIER),
      Map.entry("UInt64", UINT64_IDENTIFIER),
      Map.entry("UInteger", UINTEGER_IDENTIFIER)
  );

  private static final BootstrapConfiguration bootstrapConfiguration1 = new BootstrapConfiguration("127.0.0.1", 1251);
  private static final BootstrapConfiguration bootstrapConfiguration2 = new BootstrapConfiguration("127.0.0.1", 1261);

  public static void main(String[] args) throws ExecutionException, InterruptedException, PlcConnectionException, TimeoutException {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");

    ByteString uuid = ByteString.copyFromUtf8(UUID.randomUUID().toString());

    Adapter serverAdapter = Adapter.create(bootstrapConfiguration1);
    SfscServiceApi serverSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(serverAdapter);

    Adapter clientAdapter = Adapter.create(bootstrapConfiguration2);
    SfscServiceApi clientSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(clientAdapter);

    Plc4XServer plc4XServer = new Plc4XServer(SERVER_URL);

    /////////////////////////////////////

    SfscPublisher boolPublisher = serverSfscServiceApi
        .publisher("Bool", ByteString.copyFromUtf8(UUID.randomUUID().toString()), ByteString.copyFromUtf8("type"), Map.of("id", uuid));
    SfscPublisher byteStringPublisher = serverSfscServiceApi
        .publisher("ByteString", ByteString.copyFromUtf8(UUID.randomUUID().toString()), ByteString.copyFromUtf8("type"), Map.of("id", uuid));
    SfscPublisher bytePublisher = serverSfscServiceApi
        .publisher("Byte", ByteString.copyFromUtf8(UUID.randomUUID().toString()), ByteString.copyFromUtf8("type"), Map.of("id", uuid));
    SfscPublisher int16Publisher = serverSfscServiceApi
        .publisher("Int16", ByteString.copyFromUtf8(UUID.randomUUID().toString()), ByteString.copyFromUtf8("type"), Map.of("id", uuid));
    SfscPublisher stringPublisher = serverSfscServiceApi
        .publisher("String", ByteString.copyFromUtf8(UUID.randomUUID().toString()), ByteString.copyFromUtf8("type"), Map.of("id", uuid));

    PlcSubscriptionResponse boolSubscriptionResponse = plc4XServer.subscribe(topics.get("Bool"));
    PlcSubscriptionResponse byteStringSubscriptionResponse = plc4XServer.subscribe(topics.get("ByteString"));
    PlcSubscriptionResponse byteSubscriptionResponse = plc4XServer.subscribe(topics.get("Byte"));
    PlcSubscriptionResponse int16SubscriptionResponse = plc4XServer.subscribe(topics.get("Int16"));
    PlcSubscriptionResponse stringSubscriptionResponse = plc4XServer.subscribe(topics.get("String"));

    plc4XServer
        .register(event -> boolPublisher.publish(ByteString.copyFromUtf8(event.toString())), boolSubscriptionResponse.getSubscriptionHandles());
    plc4XServer.register(event -> byteStringPublisher.publish(ByteString.copyFromUtf8(event.toString())),
        byteStringSubscriptionResponse.getSubscriptionHandles());
    plc4XServer
        .register(event -> bytePublisher.publish(ByteString.copyFromUtf8(event.toString())), byteSubscriptionResponse.getSubscriptionHandles());
    plc4XServer
        .register(event -> int16Publisher.publish(ByteString.copyFromUtf8(event.toString())), int16SubscriptionResponse.getSubscriptionHandles());
    plc4XServer
        .register(event -> stringPublisher.publish(ByteString.copyFromUtf8(event.toString())), stringSubscriptionResponse.getSubscriptionHandles());

    Thread.sleep(1000);

    Map<String, ByteString> pubTags = clientSfscServiceApi.getServices("Bool").stream()
        .filter(new FilterFactory().byteStringEqualsFilter("id", uuid))
        .findAny().orElseThrow();

    SfscSubscriber subscriber = clientSfscServiceApi.subscriber(pubTags, bytestring -> System.out.println("Received message on subscriber"));

    ////////////////////////////////////////////////////////////

    SfscServer server = serverSfscServiceApi.server("myServer",
        ByteString.copyFromUtf8("plc4xtype"),
        ByteString.copyFromUtf8(UUID.randomUUID().toString()),
        ByteString.copyFromUtf8("plc4xtype"),

        RegexDefinition.newBuilder()
            .addRegexes(VarRegex.newBuilder()
                .setVarName("name")
                .setStringRegex(StringRegex.newBuilder().setRegex("w-String").build())
                .build())
            .addRegexes(VarRegex.newBuilder()
                .setVarName("value")
                .setStringRegex(StringRegex.newBuilder().setRegex(".*").build())
                .build())
            .build(),
        Map.of("id", uuid),
        plc4xServer(plc4XServer),
        1000,
        100,
        3);

    Thread.sleep(1000);

    Map<String, ByteString> writeServerTags = clientSfscServiceApi.getServices("myServer", writeRequest(), List.of("name", "value"))
        .stream()
        .filter(new FilterFactory().byteStringEqualsFilter("id", uuid))
        .findAny().orElseThrow();
    Map<String, ByteString> readServerTags = clientSfscServiceApi.getServices("myServer")
        .stream()
        .filter(new FilterFactory().byteStringEqualsFilter("id", uuid))
        .findAny().orElseThrow();

    SfscClient client = clientSfscServiceApi.client();
    client.request(writeServerTags, writeRequest().toByteString(), writeConsumer(), 1000, () -> System.out.println("timeout"));
    client.request(readServerTags, readRequest().toByteString(), readConsumer(), 1000, () -> System.out.println("timeout"));

    ////////////////////////////////
    ChannelGenerator channelGenerator = new ChannelGenerator(clientSfscServiceApi);
    SfscServer channelGeneratorServer = serverSfscServiceApi.channelGenerator(
        "myGen",
        Map.of("id", uuid),
        ByteString.copyFromUtf8(UUID.randomUUID().toString()),
        ByteString.copyFromUtf8("ignored"),
        channelGenerator);

    Thread.sleep(1000);
    Map<String, ByteString> genServerTags = clientSfscServiceApi.getServices("myGen").stream()
        .filter(new FilterFactory().byteStringEqualsFilter("id", uuid))
        .findAny().orElseThrow();
    SfscClient client2 = clientSfscServiceApi.client();
    Thread.sleep(1000);
    SfscSubscriber sfscSubscriber = client2.requestChannel(
        genServerTags,
        ByteString.EMPTY,
        500,
        message -> System.out.println("generated subscriber received message: " + message.toStringUtf8())
    ).get();

  }

  static Message readRequest() {
    return Plc4xMessage.newBuilder()
        .setType(Plc4xMessage.Type.READ_REQUEST)
        .setName("r-String")
        .setQuery(topics.get("String"))
        .build();
  }

  static Message writeRequest() {
    return Plc4xMessage.newBuilder()
        .setType(Plc4xMessage.Type.WRITE_REQUEST)
        .setName("w-String")
        .setQuery(topics.get("String"))
        .setValue("thisIsMyTestValue")
        .build();
  }


  static Consumer<ByteString> readConsumer() {
    return response -> {
      try {
        Plc4xMessage plc4xMessage = Plc4xMessage.parseFrom(response);
        System.out.println("Read request got response: \n" + plc4xMessage);
      } catch (InvalidProtocolBufferException e) {
        e.printStackTrace();
      }
    };
  }

  static Consumer<ByteString> writeConsumer() {
    return response -> {
      try {
        Plc4xMessage plc4xMessage = Plc4xMessage.parseFrom(response);
        System.out.println("Write request got response: \n" + plc4xMessage);
      } catch (InvalidProtocolBufferException e) {
        e.printStackTrace();
      }
    };
  }

  static Function<ByteString, AckServerResult> plc4xServer(Plc4XServer plc4XServer) {
    return requestByteString -> {
      try {
        Plc4xMessage request = Plc4xMessage.parseFrom(requestByteString);
        switch (request.getType()) {
          case READ_REQUEST: {
            PlcReadResponse readResponse = plc4XServer.read(request.getName(), request.getQuery());
            return serverResult(
                Plc4xMessage.newBuilder(request)
                    .setType(Type.READ_RESPONSE)
                    .setValue(readResponse.toString())
                    .build()
                    .toByteString()
            );
          }
          case WRITE_REQUEST: {
            plc4XServer.write(request.getName(), request.getQuery(), request.getValue());
            return serverResult(
                Plc4xMessage.newBuilder(request)
                    .setType(Type.WRITE_RESPONSE)
                    .clearValue()
                    .build()
                    .toByteString()
            );
          }
          default: {
            throw new UnsupportedOperationException();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        return serverResult(Plc4xMessage.getDefaultInstance().toByteString());
      }
    };
  }

  static AckServerResult serverResult(ByteString response) {
    return new AckServerResult(
        response,
        () -> System.out.println("plc4x server acknowledge succeeded"),
        () -> System.out.println("plc4x server acknowledge didnt succeed")
    );
  }


  static class ChannelGenerator implements Function<ByteString, SfscPublisher> {

    private final SfscServiceApi sfscServiceApi;

    ChannelGenerator(SfscServiceApi sfscServiceApi) {
      this.sfscServiceApi = sfscServiceApi;
    }

    @Override
    public SfscPublisher apply(ByteString sfscMessage) {
      SfscPublisher publisher = sfscServiceApi.unregisteredPublisher(
          "channelName",
          ByteString.copyFromUtf8(UUID.randomUUID().toString()),
          ByteString.copyFromUtf8("String"),
          Collections.emptyMap());
      publisher.subscriptionFuture(() -> publisher.publish(ByteString.copyFromUtf8("myIndividualMessage")));
      return publisher;
    }
  }

}
