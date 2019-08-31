package de.unistuttgart.isw.sfsc.client.adapter.control;

import de.unistuttgart.isw.sfsc.client.adapter.control.registry.RegistryClient;
import de.unistuttgart.isw.sfsc.protocol.control.WelcomeMessage;

public interface ControlClient {

  WelcomeMessage welcomeMessage();

  RegistryClient registryClient();

}
