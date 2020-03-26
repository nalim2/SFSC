package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.java;

import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol;
import de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.SubProtocol;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.Listeners;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.OneShotRunnable;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZLoop;
import org.zeromq.ZLoop.IZLoopHandler;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;
import zmq.ZMQ;

class JmqExecutor implements NotThrowingAutoCloseable {

  private static final byte[] NOTIFICATION = {};

  private final Queue<Runnable> commandQueue = new ConcurrentLinkedQueue<>();
  private final BlockingQueue<Object> notificationQueue = new LinkedTransferQueue<>();
  private final Listeners<Runnable> shutdownListeners = new Listeners<>();
  private final AtomicBoolean closed = new AtomicBoolean();
  private final String address = "inproc://" + UUID.randomUUID();

  private final ZContext zContext;
  private final NotificationInjector notificationInjector;
  private final CommandExecutor commandExecutor;

  JmqExecutor(ZContext zContext) {
    this.zContext = zContext;
    this.commandExecutor = new CommandExecutor();
    this.notificationInjector = new NotificationInjector();
  }

  static JmqExecutor create(ZContext zContext) throws InterruptedException {
    JmqExecutor jmqExecutor = new JmqExecutor(zContext);
    jmqExecutor.commandExecutor.start();
    jmqExecutor.commandExecutor.awaitBinding();
    jmqExecutor.notificationInjector.start();
    return jmqExecutor;
  }

  public Future<ReactiveSocket> createPublisher() {
    return commandExecutor.createReactiveSocket(SocketType.XPUB, SubProtocol.frameCount());
  }

  public Future<ReactiveSocket> createSubscriber() {
    return commandExecutor.createReactiveSocket(SocketType.XSUB, DataProtocol.frameCount());
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

  void execute(Runnable runnable) {
    commandQueue.add(runnable);
    notificationQueue.add(NOTIFICATION); //notify
  }

  @Override
  public void close() {
    if (closed.compareAndSet(false, true)) {
      new Thread(() -> {
        notificationInjector.close();
        commandExecutor.close();
        zContext.close();
        shutdownListeners.forEach(Runnable::run);
      }).start();
    }
  }

  class CommandExecutor implements NotThrowingAutoCloseable {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final CountDownLatch bound = new CountDownLatch(1);

    private final ZContext commandExecutorZContext;
    private final ZLoop zLoop;

    CommandExecutor() {
      this.commandExecutorZContext = ZContext.shadow(zContext);
      this.zLoop = new ZLoop(commandExecutorZContext);
    }

    void start() {
      executorService.execute(() -> {
            final Socket receiver = commandExecutorZContext.createSocket(SocketType.PAIR);
            receiver.bind(address);
            bound.countDown();
            final PollItem pollItem = new PollItem(receiver, ZMQ.ZMQ_POLLIN);
            final IZLoopHandler handlerManager = (unused1, unused2, unused3) -> {
              try {
                receiver.recv();
                commandQueue.remove().run();
              } catch (ZMQException e) {
                JmqExecutor.this.close();
                Thread.currentThread().interrupt();
              }
              return 0;
            };
            zLoop.addPoller(pollItem, handlerManager, null);
            zLoop.start();
            commandExecutorZContext.close();
          }
      );
    }

    Future<ReactiveSocket> createReactiveSocket(SocketType type, int defaultFrameCount) {
      FutureTask<ReactiveSocket> futureTask = new FutureTask<>(() -> {
        InboxQueue inboxQueue = new InboxQueue(defaultFrameCount);
        Socket socket = commandExecutorZContext.createSocket(type);
        PollItem pollItem = new PollItem(socket, ZMQ.ZMQ_POLLIN);
        zLoop.addPoller(pollItem, inboxQueue, null);
        return new JmqSocketImpl(JmqExecutor.this::execute, socket, inboxQueue.getInbox(), () -> closeSocket(pollItem));
      });
      JmqExecutor.this.execute(futureTask);
      return futureTask;
    }

    private void closeSocket(PollItem pollItem) {
      zLoop.removePoller(pollItem);
      commandExecutorZContext.destroySocket(pollItem.getSocket());
    }

    void awaitBinding() throws InterruptedException {
      bound.await();
    }

    @Override
    public void close() {
      executorService.shutdownNow();
    }
  }

  class NotificationInjector implements NotThrowingAutoCloseable {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    void start() {
      executorService.execute(() -> {
            ZContext notificationInjectorZContext = ZContext.shadow(zContext);
            final Socket notificationSender = notificationInjectorZContext.createSocket(SocketType.PAIR);
            notificationSender.connect(address);
            while (!Thread.interrupted()) {
              try {
                notificationQueue.take();
                notificationSender.send(NOTIFICATION);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              } catch (ZMQException e) {
                JmqExecutor.this.close();
                Thread.currentThread().interrupt();
              }
            }
            notificationInjectorZContext.close();
          }
      );
    }

    @Override
    public void close() {
      executorService.shutdownNow();
    }
  }

}
