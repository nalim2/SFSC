package servicepatterns.api;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import java.util.Map;

public interface SfscServer extends NotThrowingAutoCloseable {

  Map<String, ByteString> getTags();

  @Override
  void close();
}
