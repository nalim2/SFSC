package servicepatterns.api;

import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.adapter.AdapterParameter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public final class SfscServiceApiFactory {

  private SfscServiceApiFactory() {
  }

  public static SfscServiceApi getSfscServiceApi(AdapterParameter configuration)
      throws InterruptedException, ExecutionException, TimeoutException {
    Adapter adapter = Adapter.create(configuration);
    return new SfscServiceApiImplementation(adapter);
  }
}
