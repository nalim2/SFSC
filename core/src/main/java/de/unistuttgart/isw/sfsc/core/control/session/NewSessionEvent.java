package de.unistuttgart.isw.sfsc.core.control.session;

import com.google.protobuf.ByteString;

class NewSessionEvent {

  private final String adapterId;
  private final ByteString heartbeatAdapterTopic;

  NewSessionEvent(String adapterId, ByteString heartbeatAdapterTopic) {
    this.adapterId = adapterId;
    this.heartbeatAdapterTopic = heartbeatAdapterTopic;
  }

  public String getAdapterId() {
    return adapterId;
  }

  public ByteString getHeartbeatAdapterTopic() {
    return heartbeatAdapterTopic;
  }
}

