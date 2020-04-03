package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.jni;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownCallback {

  private static final Logger logger = LoggerFactory.getLogger(ShutdownCallback.class);

  void shutdownCallback(){
    logger.error("shutdown callback executed");
  }
}
