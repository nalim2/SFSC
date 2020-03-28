package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.jni;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownHandler {

  private static final Logger logger = LoggerFactory.getLogger(ShutdownHandler.class);

  void shutdownEvent(){
    logger.error("shutdown");
  }
}
