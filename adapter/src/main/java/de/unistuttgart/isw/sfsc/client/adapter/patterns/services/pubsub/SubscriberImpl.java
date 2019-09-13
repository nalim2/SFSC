package de.unistuttgart.isw.sfsc.client.adapter.patterns.services.pubsub;

import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.client.adapter.patterns.services.BaseService;
import java.util.Map;

class SubscriberImpl extends BaseService implements Subscriber {

  SubscriberImpl(Map<String, ByteString> tags, Runnable closer) {
    super(tags, closer);
  }
}
