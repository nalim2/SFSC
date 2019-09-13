package de.unistuttgart.isw.sfsc.client.adapter.raw.control.registry;

import com.google.protobuf.ByteString;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

public interface RegistryClient {

  //todo check if this works for multiple maps with same tags. i guess not because of hashcode() equals when entries equal. wrap into object
  Future<Map<String, ByteString>> addService(Map<String, ByteString> service);

  Future<Set<Map<String, ByteString>>> getServices();

  Future<Void> removeService(Map<String, ByteString> service);

}
