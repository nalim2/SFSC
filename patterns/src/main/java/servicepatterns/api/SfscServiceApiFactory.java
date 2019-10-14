package servicepatterns.api;

import de.unistuttgart.isw.sfsc.adapter.Adapter;

public final class SfscServiceApiFactory {

  SfscServiceApiFactory() {
  }

  public static SfscServiceApi getSfscServiceApi(Adapter adapter) {
    return new SfscServiceApiImplementation(adapter);
  }
}
