package de.unistuttgart.isw.sfsc.plc4x;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.client.adapter.raw.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.client.adapter.raw.RawAdapter;
import de.unistuttgart.isw.sfsc.example.plc4x.messages.Plc4xMessage;
import de.unistuttgart.isw.sfsc.example.plc4x.messages.Plc4xMessage.Type;
import de.unistuttgart.isw.sfsc.patterns.SfscError;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.ServiceFactory;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.ServiceFactoryImpl;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.SfscMessage;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub.Publisher;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub.Subscriber;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.reqrep.Client;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.reqrep.Server;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.tags.FilterFactory;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;

public class Main {

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
    ExecutorService executorService = Executors.newCachedThreadPool();

    RawAdapter serverRawAdapter = RawAdapter.create(bootstrapConfiguration1);
    ServiceFactory serverServiceFactory = new ServiceFactoryImpl(serverRawAdapter);

    RawAdapter clientRawAdapter = RawAdapter.create(bootstrapConfiguration2);
    ServiceFactory clientServiceFactory = new ServiceFactoryImpl(clientRawAdapter);

    Plc4XServer plc4XServer = new Plc4XServer(SERVER_URL);

    /////////////////////////////////////

    Publisher boolPublisher = serverServiceFactory.publisher(Map.ofEntries(
        Map.entry("value", ByteString.copyFromUtf8("Bool")),
        Map.entry("id", uuid)));
    Publisher byteStringPublisher = serverServiceFactory.publisher(Map.ofEntries(
        Map.entry("value", ByteString.copyFromUtf8("ByteString")),
        Map.entry("id", uuid)));
    Publisher bytePublisher = serverServiceFactory.publisher(Map.ofEntries(
        Map.entry("value", ByteString.copyFromUtf8("Byte")),
        Map.entry("id", uuid)));
    Publisher int16Publisher = serverServiceFactory.publisher(Map.ofEntries(
        Map.entry("value", ByteString.copyFromUtf8("Int16")),
        Map.entry("id", uuid)));
    Publisher stringPublisher = serverServiceFactory.publisher(Map.ofEntries(
        Map.entry("value", ByteString.copyFromUtf8("String")),
        Map.entry("id", uuid)));
    PlcSubscriptionResponse boolSubscriptionResponse = plc4XServer.subscribe(topics.get("Bool"));
    PlcSubscriptionResponse byteStringSubscriptionResponse = plc4XServer.subscribe(topics.get("ByteString"));
    PlcSubscriptionResponse byteSubscriptionResponse = plc4XServer.subscribe(topics.get("Byte"));
    PlcSubscriptionResponse int16SubscriptionResponse = plc4XServer.subscribe(topics.get("Int16"));
    PlcSubscriptionResponse stringSubscriptionResponse = plc4XServer.subscribe(topics.get("String"));
    plc4XServer.register(event -> boolPublisher.publish(event.toString().getBytes()), boolSubscriptionResponse.getSubscriptionHandles());
    plc4XServer.register(event -> byteStringPublisher.publish(event.toString().getBytes()), byteStringSubscriptionResponse.getSubscriptionHandles());
    plc4XServer.register(event -> bytePublisher.publish(event.toString().getBytes()), byteSubscriptionResponse.getSubscriptionHandles());
    plc4XServer.register(event -> int16Publisher.publish(event.toString().getBytes()), int16SubscriptionResponse.getSubscriptionHandles());
    plc4XServer.register(event -> stringPublisher.publish(event.toString().getBytes()), stringSubscriptionResponse.getSubscriptionHandles());
    serverServiceFactory.register(boolPublisher);
    serverServiceFactory.register(byteStringPublisher);
    serverServiceFactory.register(bytePublisher);
    serverServiceFactory.register(int16Publisher);
    serverServiceFactory.register(stringPublisher);

    Thread.sleep(1000);

    Map<String, ByteString> pubTags = clientServiceFactory.getServices().get()
        .stream()
        .filter(FilterFactory.publishers())
        .filter(FilterFactory.byteStringFilter("id", uuid::equals))
        .filter(FilterFactory.stringEqualsFilter("value", "String"))
        .findAny().orElseThrow();

    Subscriber subscriber = clientServiceFactory
        .subscriber(pubTags, sfscMessage -> System.out.println("Received subscription " + sfscMessage.getPayload().toStringUtf8()), executorService);

    ////////////////////////////////////////////////////////////

    Server server = serverServiceFactory.server(
        Map.ofEntries(
            Map.entry("serverName", ByteString.copyFromUtf8("myServer")),
            Map.entry("id", uuid)
        ),
        plc4xserver(plc4XServer),
        executorService);
    serverServiceFactory.register(server);

    Thread.sleep(1000);

    Map<String, ByteString> serverTags = clientServiceFactory.getServices().get()
        .stream()
        .filter(FilterFactory.servers())
        .filter(FilterFactory.byteStringFilter("id", uuid::equals))
        .filter(FilterFactory.stringEqualsFilter("serverName", "myServer"))
        .findAny().orElseThrow();

    Client client = serverServiceFactory.client(serverTags, executorService);
    client.send(writeRequest(), writeConsumer(), 1000);
    client.send(readRequest(), readConsumer(), 1000);
  }


  static byte[] readRequest() {
    return Plc4xMessage.newBuilder()
        .setType(Plc4xMessage.Type.READ_REQUEST)
        .setName("r-String")
        .setQuery(topics.get("String"))
        .build().toByteArray();
  }

  static byte[] writeRequest() {
    return Plc4xMessage.newBuilder()
        .setType(Plc4xMessage.Type.WRITE_REQUEST)
        .setName("w-String")
        .setQuery(topics.get("String"))
        .setValue(ByteString.copyFromUtf8("thisIsMyTestValue"))
        .build().toByteArray();
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

  static Function<SfscMessage, byte[]> plc4xserver(Plc4XServer plc4XServer) {
    return sfscMessage -> {
      try {
        Plc4xMessage request = Plc4xMessage.parseFrom(sfscMessage.getPayload());
        switch (request.getType()) {
          case READ_REQUEST: {
            PlcReadResponse response = plc4XServer.read(request.getName(), request.getQuery());
            return Plc4xMessage.newBuilder(request)
                .setType(Type.READ_RESPONSE)
                .setValue(ByteString.copyFromUtf8(response.toString()))
                .build()
                .toByteArray();
          }
          case WRITE_REQUEST: {
            plc4XServer.write(request.getName(), request.getQuery(), request.getValue().toStringUtf8());
            return Plc4xMessage.newBuilder(request)
                .setType(Type.WRITE_RESPONSE)
                .clearValue()
                .build()
                .toByteArray();
          }
          default: {
            throw new UnsupportedOperationException();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        return Plc4xMessage.getDefaultInstance().toByteArray();
      }
    };
  }
}
