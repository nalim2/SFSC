package de.unistuttgart.isw.sfsc.plc4x;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.adapter.base.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.example.plc4x.messages.Plc4xMessage;
import de.unistuttgart.isw.sfsc.example.plc4x.messages.Plc4xMessage.Type;
import de.unistuttgart.isw.sfsc.patterns.RegexDefinition;
import de.unistuttgart.isw.sfsc.patterns.RegexDefinition.VarRegex;
import de.unistuttgart.isw.sfsc.patterns.RegexDefinition.VarRegex.StringRegex;
import de.unistuttgart.isw.sfsc.patterns.SfscError;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import servicepatterns.FilterFactory;
import servicepatterns.Service;
import servicepatterns.ServiceApi;
import servicepatterns.ServiceApiImpl;
import servicepatterns.SfscMessage;
import servicepatterns.pubsub.Publisher;
import servicepatterns.reqrep.Client;
import servicepatterns.topicfactoryservice.TopicFactoryService;

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

  public static void main(String[] args) throws ExecutionException, InterruptedException, PlcConnectionException {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");

    ByteString uuid = ByteString.copyFromUtf8(UUID.randomUUID().toString());

    Adapter serverAdapter = Adapter.create(bootstrapConfiguration1);
    ServiceApi serverServiceApi = new ServiceApiImpl(serverAdapter);

    Adapter clientAdapter = Adapter.create(bootstrapConfiguration2);
    ServiceApi clientServiceApi = new ServiceApiImpl(clientAdapter);

    Plc4XServer plc4XServer = new Plc4XServer(SERVER_URL);

    /////////////////////////////////////

    Publisher boolPublisher = serverServiceApi.addPublisher("Bool", "rawString", Map.of("id", uuid));
    Publisher byteStringPublisher = serverServiceApi.addPublisher("ByteString", "rawString", Map.of("id", uuid));
    Publisher bytePublisher = serverServiceApi.addPublisher("Byte", "rawString", Map.of("id", uuid));
    Publisher int16Publisher = serverServiceApi.addPublisher("Int16", "rawString", Map.of("id", uuid));
    Publisher stringPublisher = serverServiceApi.addPublisher("String", "rawString", Map.of("id", uuid));

    PlcSubscriptionResponse boolSubscriptionResponse = plc4XServer.subscribe(topics.get("Bool"));
    PlcSubscriptionResponse byteStringSubscriptionResponse = plc4XServer.subscribe(topics.get("ByteString"));
    PlcSubscriptionResponse byteSubscriptionResponse = plc4XServer.subscribe(topics.get("Byte"));
    PlcSubscriptionResponse int16SubscriptionResponse = plc4XServer.subscribe(topics.get("Int16"));
    PlcSubscriptionResponse stringSubscriptionResponse = plc4XServer.subscribe(topics.get("String"));
    plc4XServer.register(event -> boolPublisher.publish(ByteString.copyFromUtf8(event.toString())), boolSubscriptionResponse.getSubscriptionHandles());
    plc4XServer.register(event -> byteStringPublisher.publish(ByteString.copyFromUtf8(event.toString())), byteStringSubscriptionResponse.getSubscriptionHandles());
    plc4XServer.register(event -> bytePublisher.publish(ByteString.copyFromUtf8(event.toString())), byteSubscriptionResponse.getSubscriptionHandles());
    plc4XServer.register(event -> int16Publisher.publish(ByteString.copyFromUtf8(event.toString())), int16SubscriptionResponse.getSubscriptionHandles());
    plc4XServer.register(event -> stringPublisher.publish(ByteString.copyFromUtf8(event.toString())), stringSubscriptionResponse.getSubscriptionHandles());

    Thread.sleep(1000);

    Map<String, ByteString> pubTags = clientServiceApi.getServices("Bool").stream()
        .filter(FilterFactory.byteStringEqualsFilter("id", uuid))
        .findAny().orElseThrow();

    Service subscriber = clientServiceApi
        .subscriber(pubTags, sfscMessage -> System.out.println("Received subscription " + sfscMessage.getPayload().toStringUtf8()));

    ////////////////////////////////////////////////////////////

    Service server = serverServiceApi.server("myServer", "plc4xtype", "plc4xtype",

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
        plc4xserver(plc4XServer));

    Thread.sleep(1000);

    Map<String, ByteString> writeServerTags = clientServiceApi.getServices("myServer", writeRequest(), List.of("name", "value")).stream()
        .filter(FilterFactory.byteStringEqualsFilter("id", uuid)).findAny().orElseThrow();
    Map<String, ByteString> readServerTags = clientServiceApi.getServices("myServer").stream()
        .filter(FilterFactory.byteStringEqualsFilter("id", uuid)).findAny().orElseThrow();

    Client client = clientServiceApi.client();
    client.send(writeServerTags, writeRequest().toByteString(), writeConsumer(), 1000);
    client.send(readServerTags, readRequest().toByteString(), readConsumer(), 1000);

    ////////////////////////////////

    TopicFactoryService topicFactoryService = clientServiceApi.addTopicGenerator("myGen", Map.of("id", uuid));
    Thread.sleep(1000);
    Map<String, ByteString> genServerTags = clientServiceApi.getServices("myGen").stream()
        .filter(FilterFactory.byteStringEqualsFilter("id", uuid)).findAny().orElseThrow();
    Client client2 = clientServiceApi.client();
    Thread.sleep(1000);
    Map<String, ByteString> publisherTopic = clientServiceApi.requestTopic(client2, genServerTags, 500).get();
    clientServiceApi.subscriber(publisherTopic, sfscMessage -> System.out.println(sfscMessage.getPayload().toStringUtf8()));
    Thread.sleep(1000);
    topicFactoryService.getPublishers().stream().findAny().orElseThrow().publish(ByteString.copyFromUtf8("myIndividualMessage"));
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


  static Consumer<SfscMessage> readConsumer() {
    return response -> {
      try {
        if (response.getError() != SfscError.NO_ERROR) {
          throw new Exception(response.getError().name());
        }
        Plc4xMessage plc4xMessage = Plc4xMessage.parseFrom(response.getPayload());
        System.out.println("Received read response: \n" + plc4xMessage);
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
  }

  static Consumer<SfscMessage> writeConsumer() {
    return response -> {
      try {
        if (response.getError() != SfscError.NO_ERROR) {
          throw new Exception(response.getError().name());
        }
        Plc4xMessage plc4xMessage = Plc4xMessage.parseFrom(response.getPayload());
        System.out.println("Received write response:  \n" + plc4xMessage);
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
  }

  static Function<SfscMessage, ByteString> plc4xserver(Plc4XServer plc4XServer) {
    return sfscMessage -> {
      try {
        Plc4xMessage request = Plc4xMessage.parseFrom(sfscMessage.getPayload());
        switch (request.getType()) {
          case READ_REQUEST: {
            PlcReadResponse response = plc4XServer.read(request.getName(), request.getQuery());
            return Plc4xMessage.newBuilder(request)
                .setType(Type.READ_RESPONSE)
                .setValue(response.toString())
                .build()
                .toByteString();
          }
          case WRITE_REQUEST: {
            plc4XServer.write(request.getName(), request.getQuery(), request.getValue());
            return Plc4xMessage.newBuilder(request)
                .setType(Type.WRITE_RESPONSE)
                .clearValue()
                .build()
                .toByteString();
          }
          default: {
            throw new UnsupportedOperationException();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        return Plc4xMessage.getDefaultInstance().toByteString();
      }
    };
  }
}
