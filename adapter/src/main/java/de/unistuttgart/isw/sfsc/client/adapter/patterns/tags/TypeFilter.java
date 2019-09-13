package de.unistuttgart.isw.sfsc.client.adapter.patterns.tags;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor.ServiceType;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor.Tags;
import java.util.Map;
import java.util.function.Predicate;

public class TypeFilter implements Predicate<Map<String, ByteString>> {

 private final Predicate<Map<String, ByteString>> predicate;

  TypeFilter(ServiceType type){
    predicate=  FilterFactory.stringEqualsFilter(Tags.TYPE.name(), type.name());
  }

  @Override
  public boolean test(Map<String, ByteString> tags) {
    return predicate.test(tags);
  }
}
