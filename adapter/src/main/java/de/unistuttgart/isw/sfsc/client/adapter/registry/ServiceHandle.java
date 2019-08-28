package de.unistuttgart.isw.sfsc.client.adapter.registry;

public class ServiceHandle {

  private final String topic;
  private final String name;

  public ServiceHandle(String topic, String name) {
    this.topic = topic;
    this.name = name;
  }

  public String getTopic() {
    return topic;
  }

  public String getName() {
    return name;
  }
}
