package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor;

import org.zeromq.ZContext;

public interface ContextConfiguration {

  void configure(ZContext context);
}
