package zmq.reactor;

import static zmq.ZError.ETERM;
import static zmq.ZMQ.ZMQ_POLLIN;

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
import zmq.reactor.ReactiveSocket.Inbox;

class Worker implements AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(Worker.class);
  private static final Supplier<Integer> threadCounter = new AtomicInteger()::getAndIncrement;
  private static final int TERMINATION_TICK_MS = 50;

  private final ZContext zContext;
  private final CommandInjectionService commandInjector;
  private final CommandExecutionService commandExecutor;

  Worker(ZContext zContext, CommandInjectionService commandInjector, CommandExecutionService commandExecutor) {
    this.zContext = zContext;
    this.commandInjector = commandInjector;
    this.commandExecutor = commandExecutor;
  }

  static Worker create(ZContext zContext) throws InterruptedException {
    int threadCount = threadCounter.get();
    CommandExecutionService commandExecutor = CommandExecutionService.create(ZContext.shadow(zContext), "ZMQ Execution Thread " + threadCount);
    commandExecutor.awaitBinding();
    CommandInjectionService commandInjector = CommandInjectionService
        .create(ZContext.shadow(zContext), commandExecutor.getQueue(), commandExecutor.getAddress(), "ZMQ Injection Thread " + threadCount);
    return new Worker(zContext, commandInjector, commandExecutor);
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

    static CommandExecutionService create(ZContext zContext, String threadName) {
      CommandExecutionService commandExecutionService = new CommandExecutionService(zContext);
      commandExecutionService.start(threadName);
      return commandExecutionService;
    }

    void start(String threadName) {
      ExecutorService serviceExecutor = Executors.newSingleThreadExecutor();
      serviceExecutor.execute(() -> {
            try {
              Thread.currentThread().setName(threadName);
              final Socket receiver = commandExecutorZContext.createSocket(SocketType.PAIR);
              receiver.bind(address);
              bound.countDown();
              final PollItem pollItem = new PollItem(receiver, ZMQ_POLLIN);
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
                  logger.warn("Unexpected Exception", e);
                }
                return 0;
              };
              zLoop.addPoller(pollItem, handlerManager, null);
              int result = zLoop.start();
              commandExecutorZContext.destroySocket(receiver);
              commandExecutorZContext.close();
              if (result != 0) {
                logger.error("Unexpected reactor shutdown with error code {}", result);
              }
              logger.debug("{} finished!", Thread.currentThread().getName());
            } catch (Exception e) {
              logger.error("Unexpected Exception", e);
            }
          }
      );
      serviceExecutor.shutdown();
    }

    Future<ReactiveSocket> createReactiveSocket(SocketType type, IZLoopHandler handler, Inbox inbox, Executor injectingExecutor) {
      FutureTask<ReactiveSocket> futureTask = new FutureTask<>(() -> {
        Socket socket = commandExecutorZContext.createSocket(type);
        PollItem pollItem = new PollItem(socket, ZMQ_POLLIN);
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

    static CommandInjectionService create(ZContext commandInjectorZContext, Queue<Runnable> commandExecutorQueue, String address, String threadName) {
      CommandInjectionService commandInjector = new CommandInjectionService(commandInjectorZContext);
      commandInjector.start(commandExecutorQueue, address, threadName);
      return commandInjector;
    }

    void start(Queue<Runnable> commandExecutorQueue, String address, String threadName) {
      ExecutorService serviceExecutor = Executors.newSingleThreadExecutor();
      serviceExecutor.execute(() -> {
            try {
              Thread.currentThread().setName(threadName);
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
                  if (errorCode == ETERM) {
                    Thread.currentThread().interrupt();
                  }
                }
              }
              commandInjectorZContext.destroySocket(notificationSender);
              commandInjectorZContext.close();
              receivingQueue.clear();
              logger.debug("{} finished!", Thread.currentThread().getName());
            } catch (Exception e) {
              logger.error("Unexpected Exception", e);
            }
          }
      );
      serviceExecutor.shutdown();
    }

    void queue(Runnable runnable) {
      receivingQueue.add(runnable);
    }

  }
}
