package servicepatterns.api.filtering;

import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import java.util.Collection;
import java.util.function.Predicate;

public final class Filters {

  private Filters() {}

  public static Predicate<SfscServiceDescriptor> regexFilter(Message message, String varPath) {
    return new RegexFilter(message, varPath);
  }

  public static Predicate<SfscServiceDescriptor> regexFilter(Message message, Collection<String> varPaths) {
    return varPaths
        .stream()
        .map(varPath -> regexFilter(message, varPath))
        .reduce(Predicate::and)
        .orElseThrow();
  }
}
