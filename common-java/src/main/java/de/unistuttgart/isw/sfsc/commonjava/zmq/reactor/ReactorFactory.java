package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor;

import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.java.JmqReactor;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.jni.JniReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReactorFactory {

  private static final Logger logger = LoggerFactory.getLogger(ReactorFactory.class);
  static final boolean useNative;

  static {
    boolean useNativeTemp;
    try {
      System.loadLibrary("ZmqExecutorLib");
      System.loadLibrary("JniZmq");
      useNativeTemp = true;
    } catch (UnsatisfiedLinkError e) {
      e.printStackTrace(); //todo
      useNativeTemp = false;
    }
    useNative = useNativeTemp;
    logger.info("using native lib: " + useNative);
  }

  public static Reactor create() throws InterruptedException {
    return useNative ? JniReactor.create() : JmqReactor.create();
  }

  public static boolean usesNative() {
    return useNative;
  }
}
