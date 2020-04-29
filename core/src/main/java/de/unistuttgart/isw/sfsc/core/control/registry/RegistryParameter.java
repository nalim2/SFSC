package de.unistuttgart.isw.sfsc.core.control.registry;

import com.google.protobuf.ByteString;

public class RegistryParameter {

  private final ByteString queryServerTopic;
  private final ByteString commandServerTopic;
  private final ByteString eventPublisherTopic;

  public RegistryParameter(String queryServerTopic, String commandServerTopic, String eventPublisherTopic) {
    this.queryServerTopic = ByteString.copyFromUtf8(queryServerTopic);
    this.commandServerTopic = ByteString.copyFromUtf8(commandServerTopic);
    this.eventPublisherTopic = ByteString.copyFromUtf8(eventPublisherTopic);
  }

  public ByteString getQueryServerTopic() {
    return queryServerTopic;
  }

  public ByteString getCommandServerTopic() {
    return commandServerTopic;
  }

  public ByteString getEventPublisherTopic() {
    return eventPublisherTopic;
  }
}
