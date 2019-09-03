package de.unistuttgart.isw.sfsc.client.adapter.control.registry;

import com.google.protobuf.ByteString;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

public interface RegistryClient {

  Future<Map<String, ByteString>> addService(Map<String, ByteString> service);

  Future<Set<Map<String, ByteString>>> getServices();

  Future<Void> removeService(Map<String, ByteString> service);

}
