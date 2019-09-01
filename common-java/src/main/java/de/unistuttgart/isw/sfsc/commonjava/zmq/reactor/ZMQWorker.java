package de.unistuttgart.isw.sfsc.commonjava.zmq.reactor;

import de.unistuttgart.isw.sfsc.commonjava.util.ExceptionLoggingThreadFactory;
import de.unistuttgart.isw.sfsc.commonjava.zmq.reactor.ReactiveSocket.Inbox;
import java.lang.invoke.MethodHandles;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZLoop;
import org.zeromq.ZLoop.IZLoopHandler;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;
import zmq.ZError;
import zmq.ZMQ;

class ZMQWorker implements AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(ZMQWorker.class);
  private static final Supplier<Integer> workerCounter = new AtomicInteger()::getAndIncrement;
  private static final int TERMINATION_TICK_MS = 50;

  private final ZContext zContext;
  private final CommandInjectionService commandInjector;
  private final CommandExecutionService commandExecutor;

  ZMQWorker(ZContext zContext, CommandInjectionService commandInjector, CommandExecutionService commandExecutor) {
    this.zContext = zContext;
    this.commandInjector = commandInjector;
    this.commandExecutor = commandExecutor;
  }

  static ZMQWorker create(ZContext zContext) throws InterruptedException {
    String workerName = MethodHandles.lookup().lookupClass().getName() + "-" + workerCounter.get();
    CommandExecutionService commandExecutor = CommandExecutionService.create(ZContext.shadow(zContext), workerName);
    commandExecutor.awaitBinding();
    CommandInjectionService commandInjector = CommandInjectionService
        .create(ZContext.shadow(zContext), commandExecutor.getQueue(), commandExecutor.getAddress(), workerName);
    return new ZMQWorker(zContext, commandInjector, commandExecutor);
  }

  Future<ReactiveSocket> createSocket(SocketType type, IZLoopHandler handler, Inbox inbox) {
    return commandExecutor.createReactiveSocket(type, handler, inbox, commandInjector::queue);
  }

  @Override
  public void close() {
    try {
      Thread contextTermination = new Thread(zContext::close);
      contextTermination.start();
      while (contextTermination.isAlive()) {
        logger.debug("Waiting for zContext termination");
        commandInjector.queue(() -> {
        });
        contextTermination.join(TERMINATION_TICK_MS);
      }
    } catch (Exception e) {
      logger.error("Exception while closing", e);
    }
  }


  static class CommandExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutionService.class);

    private final Queue<Runnable> commandQueue = new ConcurrentLinkedQueue<>();
    private final CountDownLatch bound = new CountDownLatch(1);
    private final String address = "inproc://" + UUID.randomUUID();

    private final ZContext commandExecutorZContext;
    private final ZLoop zLoop;

    CommandExecutionService(ZContext commandExecutorZContext) {
      this.zLoop = new ZLoop(commandExecutorZContext);
      this.commandExecutorZContext = commandExecutorZContext;
    }

    static CommandExecutionService create(ZContext zContext, String workerName) {
      CommandExecutionService commandExecutionService = new CommandExecutionService(zContext);
      commandExecutionService.start(workerName);
      return commandExecutionService;
    }

    void start(String workerName) {
      String threadGroupName = workerName + "." + getClass().getName();
      ExecutorService serviceExecutor = Executors.newSingleThreadExecutor(new ExceptionLoggingThreadFactory(threadGroupName, logger));
      serviceExecutor.execute(() -> {
            final Socket receiver = commandExecutorZContext.createSocket(SocketType.PAIR);
            receiver.bind(address);
            bound.countDown();
            final PollItem pollItem = new PollItem(receiver, ZMQ.ZMQ_POLLIN);
            final IZLoopHandler handlerManager = (unused1, unused2, unused3) -> {
              try {
                Runnable runnable = commandQueue.poll();
                if (runnable != null) {
                  receiver.recv();
                  try {
                    runnable.run();
                  } catch (Exception e) {
                    logger.warn("Execution of Runnable failed with Exception", e);
                  }
                }
              } catch (Exception e) {
                logger.warn("Unexpected Exception in thread " + Thread.currentThread().getName(), e);
              }
              return 0;
            };
            zLoop.addPoller(pollItem, handlerManager, null);
            int result = zLoop.start();
            commandExecutorZContext.destroySocket(receiver);
            commandExecutorZContext.close();
            if (result != 0) {
              logger.error("{} shutdown unexpectedly with error code {}", Thread.currentThread().getName(), result);
            }
            logger.debug("{} finished!", Thread.currentThread().getName());
          }
      );
      serviceExecutor.shutdown();
    }

    Future<ReactiveSocket> createReactiveSocket(SocketType type, IZLoopHandler handler, Inbox inbox, Executor injectingExecutor) {
      FutureTask<ReactiveSocket> futureTask = new FutureTask<>(() -> {
        Socket socket = commandExecutorZContext.createSocket(type);
        PollItem pollItem = new PollItem(socket, ZMQ.ZMQ_POLLIN);
        zLoop.addPoller(pollItem, handler, null);
        return new ReactiveSocketImpl(injectingExecutor, socket, inbox, () -> closeSocket(pollItem));
      });
      injectingExecutor.execute(futureTask);
      return futureTask;
    }

    private void closeSocket(PollItem pollItem) {
      zLoop.removePoller(pollItem);
      commandExecutorZContext.destroySocket(pollItem.getSocket());
    }

    String getAddress() {
      return address;
    }

    Queue<Runnable> getQueue() {
      return commandQueue;
    }

    void awaitBinding() throws InterruptedException {
      bound.await();
    }
  }


  static class CommandInjectionService {

    private static final Logger logger = LoggerFactory.getLogger(CommandInjectionService.class);

    private final BlockingQueue<Runnable> receivingQueue = new LinkedTransferQueue<>();

    private final ZContext commandInjectorZContext;

    CommandInjectionService(ZContext commandInjectorZContext) {
      this.commandInjectorZContext = commandInjectorZContext;
    }

    static CommandInjectionService create(ZContext commandInjectorZContext, Queue<Runnable> commandExecutorQueue, String address, String workerName) {
      CommandInjectionService commandInjector = new CommandInjectionService(commandInjectorZContext);
      commandInjector.start(commandExecutorQueue, address, workerName);
      return commandInjector;
    }

    void start(Queue<Runnable> commandExecutorQueue, String address, String workerName) {
      String threadGroupName = workerName + "." + getClass().getName();
      ExecutorService serviceExecutor = Executors.newSingleThreadExecutor(new ExceptionLoggingThreadFactory(threadGroupName, logger));
      serviceExecutor.execute(() -> {
            final byte[] notification = {};
            final Socket notificationSender = commandInjectorZContext.createSocket(SocketType.PAIR);
            notificationSender.connect(address);
            while (!Thread.interrupted()) {
              try {
                Runnable runnable = receivingQueue.take();
                commandExecutorQueue.add(runnable);
                notificationSender.send(notification);
              } catch (ZMQException e) {
                int errorCode = e.getErrorCode();
                if (errorCode == ZError.ETERM) {
                  Thread.currentThread().interrupt();
                }
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            }
            commandInjectorZContext.destroySocket(notificationSender);
            commandInjectorZContext.close();
            receivingQueue.clear();
            logger.debug("{} finished!", Thread.currentThread().getName());
          }
      );
      serviceExecutor.shutdown();
    }

    void queue(Runnable runnable) {
      receivingQueue.add(runnable);
    }

  }
}
