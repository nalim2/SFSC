package de.unistuttgart.isw.sfsc.dockercore;

import de.unistuttgart.isw.sfsc.core.Core;
import de.unistuttgart.isw.sfsc.dockercore.serf.SerfConfiguration;
import java.io.IOException;
import java.util.List;

public class Main {

  public static void main(String[] args) throws Exception {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");

    DockerConfiguration dockerConfiguration = DockerConfiguration.fromEnvironment();
    SerfConfiguration serfConfiguration = SerfConfiguration.fromConfiguration(dockerConfiguration);
    startNewProcess(serfConfiguration.toCommand());
    Core.start(dockerConfiguration.toCoreConfiguration());
  }

  static Process startNewProcess(List<String> commandList) throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.inheritIO();
    processBuilder.command(commandList);
    return processBuilder.start();
  }
}
