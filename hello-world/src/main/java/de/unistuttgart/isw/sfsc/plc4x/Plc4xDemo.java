package de.unistuttgart.isw.sfsc.plc4x;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import de.unistuttgart.isw.sfsc.adapter.configuration.AdapterConfiguration;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent.StoreEventType;
import de.unistuttgart.isw.sfsc.example.plc4x.messages.Plc4xMessage;
import de.unistuttgart.isw.sfsc.example.plc4x.messages.Plc4xMessage.Type;
import de.unistuttgart.isw.sfsc.framework.api.SfscChannelFactoryParameter;
import de.unistuttgart.isw.sfsc.framework.api.SfscClient;
import de.unistuttgart.isw.sfsc.framework.api.SfscPublisher;
import de.unistuttgart.isw.sfsc.framework.api.SfscPublisherParameter;
import de.unistuttgart.isw.sfsc.framework.api.SfscServer;
import de.unistuttgart.isw.sfsc.framework.api.SfscServerParameter;
import de.unistuttgart.isw.sfsc.framework.api.SfscServiceApi;
import de.unistuttgart.isw.sfsc.framework.api.SfscServiceApiFactory;
import de.unistuttgart.isw.sfsc.framework.api.SfscSubscriber;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServerTags.RegexDefinition;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServerTags.RegexDefinition.VarRegex;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor.ServerTags.RegexDefinition.VarRegex.StringRegex;
import de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep.AckServerResult;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;

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

  private static final AdapterConfiguration ADAPTER_CONFIGURATION_1 = new AdapterConfiguration().setCorePubTcpPort(1251);
  private static final AdapterConfiguration ADAPTER_CONFIGURATION_2 = new AdapterConfiguration().setCorePubTcpPort(1261);

  public static void main(String[] args) throws ExecutionException, InterruptedException, PlcConnectionException, TimeoutException {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");

    SfscServiceApi serverSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(ADAPTER_CONFIGURATION_1);
    SfscServiceApi clientSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(ADAPTER_CONFIGURATION_2);

    Plc4XServer plc4XServer = new Plc4XServer(SERVER_URL);

    /////////////////////////////////////

    SfscPublisher boolPublisher = serverSfscServiceApi.publisher(new SfscPublisherParameter().setServiceName("Bool"));
    SfscPublisher byteStringPublisher = serverSfscServiceApi.publisher(new SfscPublisherParameter().setServiceName("ByteString"));
    SfscPublisher bytePublisher = serverSfscServiceApi.publisher(new SfscPublisherParameter().setServiceName("Byte"));
    SfscPublisher int16Publisher = serverSfscServiceApi.publisher(new SfscPublisherParameter().setServiceName("Int16"));
    SfscPublisher stringPublisher = serverSfscServiceApi.publisher(new SfscPublisherParameter().setServiceName("String"));

    PlcSubscriptionResponse boolSubscriptionResponse = plc4XServer.subscribe(topics.get("Bool"));
    PlcSubscriptionResponse byteStringSubscriptionResponse = plc4XServer.subscribe(topics.get("ByteString"));
    PlcSubscriptionResponse byteSubscriptionResponse = plc4XServer.subscribe(topics.get("Byte"));
    PlcSubscriptionResponse int16SubscriptionResponse = plc4XServer.subscribe(topics.get("Int16"));
    PlcSubscriptionResponse stringSubscriptionResponse = plc4XServer.subscribe(topics.get("String"));

    plc4XServer.register(event -> boolPublisher.publish(StringValue.of(event.toString())), boolSubscriptionResponse.getSubscriptionHandles());
    plc4XServer
        .register(event -> byteStringPublisher.publish(StringValue.of(event.toString())), byteStringSubscriptionResponse.getSubscriptionHandles());
    plc4XServer.register(event -> bytePublisher.publish(StringValue.of(event.toString())), byteSubscriptionResponse.getSubscriptionHandles());
    plc4XServer.register(event -> int16Publisher.publish(StringValue.of(event.toString())), int16SubscriptionResponse.getSubscriptionHandles());
    plc4XServer.register(event -> stringPublisher.publish(StringValue.of(event.toString())), stringSubscriptionResponse.getSubscriptionHandles());

    clientSfscServiceApi.addOneShotRegistryStoreEventListener(
        event -> event.getStoreEventType() == StoreEventType.CREATE
            && Objects.equals(event.getData().getServiceName(), "Bool")
    );
    System.out.println("matching service found");
    SfscServiceDescriptor pubDescriptor = clientSfscServiceApi.getServices("Bool").stream().findAny().orElseThrow();

    SfscSubscriber subscriber = clientSfscServiceApi.subscriber(pubDescriptor, bytestring -> System.out.println("Received message on subscriber"));

    ////////////////////////////////////////////////////////////

    SfscServerParameter serverParameter = new SfscServerParameter()
        .setServiceName("myServer")
        .setInputMessageType(ByteString.copyFromUtf8("plc4xtype"))
        .setOutputMessageType(ByteString.copyFromUtf8("plc4xtype"))
        .setRegexDefinition(RegexDefinition.newBuilder()
            .addRegexes(VarRegex.newBuilder()
                .setVarName("name")
                .setStringRegex(StringRegex.newBuilder().setRegex("w-String").build())
                .build())
            .addRegexes(VarRegex.newBuilder()
                .setVarName("value")
                .setStringRegex(StringRegex.newBuilder().setRegex(".*").build())
                .build())
            .build());
    SfscServer server = serverSfscServiceApi.server(serverParameter, plc4xServer(plc4XServer));

    Thread.sleep(1000);

    SfscServiceDescriptor writeServerTags = clientSfscServiceApi.getServices("myServer", writeRequest(), List.of("name", "value"))
        .stream().findAny().orElseThrow();
    SfscServiceDescriptor readServerTags = clientSfscServiceApi.getServices("myServer")
        .stream().findAny().orElseThrow();

    SfscClient client = clientSfscServiceApi.client();
    client.request(writeServerTags, writeRequest(), writeConsumer(), 1000, () -> System.out.println("timeout"));
    client.request(readServerTags, readRequest(), readConsumer(), 1000, () -> System.out.println("timeout"));

    ////////////////////////////////
    SfscChannelFactoryParameter factoryParameter = new SfscChannelFactoryParameter()
        .setServiceName("myFactory");
    SfscServer channelFactoryServer = serverSfscServiceApi.channelFactory(factoryParameter, new ChannelFactoryFunction(clientSfscServiceApi));

    Thread.sleep(1000);
    SfscServiceDescriptor descriptor = clientSfscServiceApi.getServices("myFactory").stream().findAny().orElseThrow();
    SfscClient client2 = clientSfscServiceApi.client();
    Thread.sleep(1000);
    SfscSubscriber sfscSubscriber = client2.requestChannel(
        descriptor,
        ByteString.EMPTY,
        500,
        message -> {
          try {
            System.out.println("generated subscriber received message: " + StringValue.parseFrom(message).toString());
          } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
          }
        }
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
            );
          }
          case WRITE_REQUEST: {
            plc4XServer.write(request.getName(), request.getQuery(), request.getValue());
            return serverResult(
                Plc4xMessage.newBuilder(request)
                    .setType(Type.WRITE_RESPONSE)
                    .clearValue()
                    .build()
            );
          }
          default: {
            throw new UnsupportedOperationException();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        return serverResult(Plc4xMessage.getDefaultInstance());
      }
    };
  }

  static AckServerResult serverResult(Message response) {
    return new AckServerResult(
        response,
        () -> System.out.println("plc4x server acknowledge succeeded"),
        () -> System.out.println("plc4x server acknowledge didnt succeed")
    );
  }


  static class ChannelFactoryFunction implements Function<ByteString, SfscPublisher> {

    private final SfscServiceApi sfscServiceApi;

    ChannelFactoryFunction(SfscServiceApi sfscServiceApi) {
      this.sfscServiceApi = sfscServiceApi;
    }

    @Override
    public SfscPublisher apply(ByteString sfscMessage) {
      SfscPublisherParameter parameter = new SfscPublisherParameter()
          .setServiceName("channelName")
          .setOutputMessageType(ByteString.copyFromUtf8("String"))
          .setUnregistered(true);
      SfscPublisher publisher = sfscServiceApi.publisher(parameter);
      publisher.onSubscription(() -> publisher.publish(StringValue.of("myIndividualMessage")));
      return publisher;
    }
  }

}
