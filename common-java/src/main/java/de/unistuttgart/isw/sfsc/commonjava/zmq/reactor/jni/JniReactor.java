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

  private final AtomicBoolean closed = new AtomicBoolean();
  private final Listeners<Runnable> shutdownListeners = new Listeners<>();

  public JniReactor(long nativePointer) {this.nativePointer = nativePointer;}

  public static Reactor create() { //todo config
    ShutdownHandler shutdownHandler = new ShutdownHandler();
    long nativePointer = JniReactor.initNative(InboxQueue.class, shutdownHandler);
    return new JniReactor(nativePointer);
  }

  public ReactiveSocket createSubscriber() {
    InboxQueue inboxQueue = new InboxQueue();
    long nativeSocketPointer = JniReactor.createSubscriber(nativePointer, inboxQueue);
    return new JniReactiveSocket(nativeSocketPointer, inboxQueue);
  }

  public ReactiveSocket createPublisher() {
    InboxQueue inboxQueue = new InboxQueue();
    long nativeSocketPointer = JniReactor.createPublisher(nativePointer, inboxQueue);
    return new JniReactiveSocket(nativeSocketPointer, inboxQueue);
  }

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
    JniReactor.close(nativePointer);
  }

  //if u refactor method names, you also need to change native part
  static native long initNative(Class<? extends InboxQueue> inboxClass, ShutdownHandler ShutdownHandler);

  static native long createSubscriber(long nativePointer, InboxQueue inboxQueue);

  static native long createPublisher(long nativePointer, InboxQueue inboxQueue);

  // todo shutdown callback
  // todo how to clean shutdown

  static native void close(long nativePointer); //todo other way round?

}
