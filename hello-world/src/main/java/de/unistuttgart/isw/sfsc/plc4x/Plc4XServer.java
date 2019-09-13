package de.unistuttgart.isw.sfsc.plc4x;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.model.PlcConsumerRegistration;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.apache.plc4x.java.api.types.PlcSubscriptionType;
import org.apache.plc4x.java.base.messages.DefaultPlcSubscriptionRequest;
import org.apache.plc4x.java.base.model.SubscriptionPlcField;
import org.apache.plc4x.java.opcua.connection.OpcuaTcpPlcConnection;
import org.apache.plc4x.java.opcua.protocol.OpcuaField;

public class Plc4XServer {

  private final OpcuaTcpPlcConnection opcuaConnection;

  public Plc4XServer(String serverUrl) throws PlcConnectionException {
    opcuaConnection = (OpcuaTcpPlcConnection) new PlcDriverManager().getConnection(serverUrl);
  }

  public PlcReadResponse read(String name, String fieldQuery) throws ExecutionException, InterruptedException {
    PlcReadRequest request = opcuaConnection.readRequestBuilder()
        .addItem(name, fieldQuery)
        .build();
    return opcuaConnection.read(request).get();
  }

  public PlcWriteResponse write(String name, String query, String value) throws ExecutionException, InterruptedException {
    PlcWriteRequest request = opcuaConnection.writeRequestBuilder()
        .addItem(name, query, value)
        .build();
    return opcuaConnection.write(request).get();
  }

  public PlcSubscriptionResponse subscribe(String identifier) throws ExecutionException, InterruptedException {
    return opcuaConnection.subscribe(new DefaultPlcSubscriptionRequest(
        opcuaConnection,
        new LinkedHashMap<>(
            Collections.singletonMap("field1",
                new SubscriptionPlcField(PlcSubscriptionType.CHANGE_OF_STATE, OpcuaField.of(identifier), Duration.of(1, ChronoUnit.SECONDS)))
        )
    )).get();
  }

  public PlcConsumerRegistration register(Consumer<PlcSubscriptionEvent> consumer, Collection<PlcSubscriptionHandle> handles) {
    return opcuaConnection.register(consumer, handles);
  }

}
