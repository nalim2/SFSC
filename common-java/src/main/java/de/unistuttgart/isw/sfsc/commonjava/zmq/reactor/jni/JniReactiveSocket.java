package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.jni;

import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.TransportProtocol;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class JniReactiveSocket implements ReactiveSocket {

  private static final byte[][] templateArray = {};
  private final long nativePointer;
  private final InboxQueue inboxQueue;
  private final AtomicBoolean closed = new AtomicBoolean();

  JniReactiveSocket(long nativePointer, InboxQueue inboxQueue) {
    this.nativePointer = nativePointer;
    this.inboxQueue = inboxQueue;
  }

  @Override
  public Inbox getInbox() {
    return inboxQueue;
  }

  @Override
  public Outbox getOutbox() {
    return new Outbox() {
      @Override
      public void add(List<byte[]> output) {
        if (!closed.get()) {
          JniReactiveSocket.add(nativePointer, output.toArray(templateArray)); //todo make outbox take array directly to tune?
        }
      }
    };
  }

  @Override
  public Connector getConnector() {
    return new Connector() {
      @Override
      public void connect(TransportProtocol protocol, String address) {
        if (!closed.get()) {
          JniReactiveSocket.connect(nativePointer, Connector.createUri(protocol, address));
        }
      }

      @Override
      public void disconnect(TransportProtocol protocol, String address) {
        if (!closed.get()) {
          JniReactiveSocket.disconnect(nativePointer, Connector.createUri(protocol, address));
        }
      }

      @Override
      public void bind(TransportProtocol protocol, String address) {
        if (!closed.get()) {
          JniReactiveSocket.bind(nativePointer, Connector.createUri(protocol, address));
        }
      }

      @Override
      public void unbind(TransportProtocol protocol, String address) {
        if (!closed.get()) {
          JniReactiveSocket.unbind(nativePointer, Connector.createUri(protocol, address));
        }
      }
    };
  }

  @Override
  public Settings getSettings() {
    return new Settings() {

      @Override
      public void setXPubVerbose() {
        if (!closed.get()) {
          JniReactiveSocket.setXPubVerbose(nativePointer);
        }
      }
    };
  }

  @Override
  public void close() {
    closed.set(true);
    JniReactiveSocket.shutdownNative(nativePointer);
  }

  void addInboxMessage(byte[][] data) {
    inboxQueue.addInboxMessage(data);
  }

  static native void add(long nativePointer, byte[][] output);

  static native void connect(long nativePointer, String address);

  static native void disconnect(long nativePointer, String address);

  static native void bind(long nativePointer, String address);

  static native void unbind(long nativePointer, String address);

  static native void setXPubVerbose(long nativePointer);

  static native void shutdownNative(long nativePointer);

}
