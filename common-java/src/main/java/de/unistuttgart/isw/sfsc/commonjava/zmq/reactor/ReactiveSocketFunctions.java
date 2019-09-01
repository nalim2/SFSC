package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ.Socket;

class ReactiveSocketFunctions {

  private static final Logger logger = LoggerFactory.getLogger(ReactiveSocketFunctions.class);
  private static final String WILDCARD_HOST = "*";

  static void connect(Socket socket, String host, int port) {
      String address = createTcpAddress(host, port);
      socket.connect(address);
      logger.debug("Connected socket {} to {}", socket, address);
  }

  static void disconnect(Socket socket, String host, int port) {
      String address = createTcpAddress(host, port);
      socket.disconnect(address);
      logger.debug("Disconnected socket {} from {}", socket, address);
  }

  static void bind(Socket socket, int port) {
      String address = createTcpWildcardAddress(port);
      socket.bind(address);
      logger.debug("Bound socket {} to {}", socket, address);
  }

  static void unbind(Socket socket, int port) {
      String address = createTcpWildcardAddress(port);
      socket.disconnect(address);
      logger.debug("Unbound socket {} from {}", socket, address);
  }

  static String createTcpAddress(String host, int port) {
    return "tcp://" + host + ":" + port;
  }

  static String createTcpWildcardAddress(int port) {
    return "tcp://" + WILDCARD_HOST + ":" + port;
  }

  static void write(Socket socket, byte[][] outputMessage) {
      for (int i = 0; i < outputMessage.length - 1; i++) {
        socket.sendMore(outputMessage[i]);
      }
      socket.send(outputMessage[outputMessage.length - 1]);
  }
}
