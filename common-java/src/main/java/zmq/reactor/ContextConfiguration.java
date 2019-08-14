package zmq.reactor;

import org.zeromq.ZContext;

public interface ContextConfiguration {

  void configure(ZContext context); //todo socketfactory
}
