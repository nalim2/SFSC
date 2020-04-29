package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.jni;

import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.OneShotRunnable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.Reactor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JniReactor implements Reactor {

  private static final Logger logger = LoggerFactory.getLogger(JniReactor.class);
  final long nativePointer;
  private final Listeners<Runnable> shutdownListeners;
  private final AtomicBoolean closed = new AtomicBoolean();

  public JniReactor(long nativePointer, Listeners<Runnable> shutdownListeners) {
    this.nativePointer = nativePointer;
    this.shutdownListeners = shutdownListeners;
  }

  public static Reactor create() { //todo config
    Listeners<Runnable> shutdownListeners = new Listeners<>();
    ShutdownCallback shutdownCallback = () -> shutdownListeners.forEach(Runnable::run);
    long nativePointer = JniReactor.createNative(shutdownCallback);
    return new JniReactor(nativePointer, shutdownListeners);
  }

  public ReactiveSocket createSubscriber() {
    InboxQueue inboxQueue = new InboxQueue();
    if (closed.get()) {
      throw new IllegalStateException("already closed");
    }
    long nativeSocketPointer = JniReactor.createSubscriber(nativePointer, inboxQueue);
    return new JniReactiveSocket(nativeSocketPointer, inboxQueue);
  }

  public ReactiveSocket createPublisher() {
    InboxQueue inboxQueue = new InboxQueue();
    if (closed.get()) {
      throw new IllegalStateException("already closed");
    }
    long nativeSocketPointer = JniReactor.createPublisher(nativePointer, inboxQueue);
    return new JniReactiveSocket(nativeSocketPointer, inboxQueue);
  }

  @Override
  public Handle addShutdownListener(Runnable runnable) {
    Runnable oneShotRunnable = new OneShotRunnable(runnable);
    Handle handle = shutdownListeners.add(oneShotRunnable);
    if (closed.get()) {
      oneShotRunnable.run();
      handle.close();
    }
    return handle;
  }

  @Override
  public void close() {
    closed.set(true);
    JniReactor.close(nativePointer);
  }

  //if u refactor method names, you also need to change native part
  static native long createNative(ShutdownCallback ShutdownCallback);

  static native long createSubscriber(long nativePointer, InboxQueue inboxQueue);

  static native long createPublisher(long nativePointer, InboxQueue inboxQueue);

  static native void close(long nativePointer);

}
