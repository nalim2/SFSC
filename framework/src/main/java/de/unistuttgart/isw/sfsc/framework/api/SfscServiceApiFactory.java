package de.unistuttgart.isw.sfsc.framework.api;

import de.unistuttgart.isw.sfsc.adapter.Adapter;
import de.unistuttgart.isw.sfsc.adapter.configuration.AdapterConfiguration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public final class SfscServiceApiFactory {

  private SfscServiceApiFactory() {
  }

  public static SfscServiceApi getSfscServiceApi() throws InterruptedException, ExecutionException, TimeoutException {
    Adapter adapter = Adapter.create(new AdapterConfiguration());
    return new SfscServiceApiImplementation(adapter);
  }

  public static SfscServiceApi getSfscServiceApi(AdapterConfiguration configuration)
      throws InterruptedException, ExecutionException, TimeoutException {
    Adapter adapter = Adapter.create(configuration);
    return new SfscServiceApiImplementation(adapter);
  }
}
