package de.unistuttgart.isw.sfsc.client.adapter.patterns.services.reqrep;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.BaseService;
import java.util.Map;

class ServerImpl extends BaseService implements Server {

  ServerImpl(Map<String, ByteString> tags, Runnable closer) {
    super(tags, closer);
  }
}
