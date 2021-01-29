package de.unistuttgart.isw.sfsc.dockercore;

import de.unistuttgart.isw.sfsc.core.Core;

public class Main {

  public static void main(String[] args) throws Exception {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
    Core.start();

  }
}
