package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.jni;

import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.ListenableEvent;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;

public class JniReactor implements Reactor {

  final long nativePointer;
  private final ListenableEvent shutdownEvent;

  public JniReactor(long nativePointer, ListenableEvent shutdownEvent) {
    this.nativePointer = nativePointer;
    this.shutdownEvent = shutdownEvent;
    shutdownEvent.addListener(new Thread(() -> JniReactor.close(nativePointer))::start);
  }

  public static Reactor create() { //todo config
    ListenableEvent shutdownEvent = new ListenableEvent();
    long nativePointer = JniReactor.createNative(shutdownEvent::fire);
    return new JniReactor(nativePointer, shutdownEvent);
  }

  public ReactiveSocket createSubscriber() {
    InboxQueue inboxQueue = new InboxQueue();
    if (shutdownEvent.isFired()) {
      throw new IllegalStateException("already closed");
    }
    long nativeSocketPointer = JniReactor.createSubscriber(nativePointer, inboxQueue);
    return new JniReactiveSocket(nativeSocketPointer, inboxQueue);
  }

  public ReactiveSocket createPublisher() {
    InboxQueue inboxQueue = new InboxQueue();
    if (shutdownEvent.isFired()) {
      throw new IllegalStateException("already closed");
    }
    long nativeSocketPointer = JniReactor.createPublisher(nativePointer, inboxQueue);
    return new JniReactiveSocket(nativeSocketPointer, inboxQueue);
  }

  public Handle addShutdownListener(Runnable runnable) {
    return shutdownEvent.addListener(runnable);
  }

  @Override
  public void close() {
    shutdownEvent.fire();
  }

  //if u refactor method names, you also need to change native part
  static native long createNative(ShutdownCallback ShutdownCallback);

  static native long createSubscriber(long nativePointer, InboxQueue inboxQueue);

  static native long createPublisher(long nativePointer, InboxQueue inboxQueue);

  static native void close(long nativePointer);

}
