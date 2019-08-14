package de.unistuttgart.isw.sfsc.core.serf;

import de.unistuttgart.isw.sfsc.core.configuration.Configuration;
import de.unistuttgart.isw.sfsc.core.configuration.CoreOption;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import no.tv2.serf.client.Client;
import no.tv2.serf.client.Member;
import no.tv2.serf.client.MembershipEvent;
import no.tv2.serf.client.SerfCommunicationException;
import no.tv2.serf.client.SocketEndpoint;
import no.tv2.serf.client.StreamSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerfEndpoint implements AutoCloseable {

  private static final Supplier<Integer> threadCounter = new AtomicInteger()::getAndIncrement;
  private static final Logger logger = LoggerFactory.getLogger(SerfEndpoint.class);

  private final ExecutorService executorService = Executors.newCachedThreadPool();
  private final Client client;

  private SerfEndpoint(Client client) {
    this.client = client;
  }

  public static SerfEndpoint getInstance(Configuration<CoreOption> configuration) throws IOException {
    no.tv2.serf.client.SerfEndpoint endpoint = new SocketEndpoint(configuration.get(CoreOption.SERF_RPC_HOST),
        Integer.parseInt(configuration.get(CoreOption.SERF_RPC_PORT)));
    Client client = establishConnection(endpoint);
    return new SerfEndpoint(client);
  }

  private static Client establishConnection(no.tv2.serf.client.SerfEndpoint endpoint) throws IOException {
    try {
      Client client = new Client(endpoint);
      client.handshake();
      return client;
    } catch (SerfCommunicationException e) {
      throw new IOException(e);
    }
  }

  public List<Member> members() throws IOException {
    try {
      return client.members().getMembers();
    } catch (SerfCommunicationException e) {
      throw new IOException(e);
    }
  }

  public StreamSubscription memberJoinStream() throws IOException {
    try {
      return client.stream("member-join");
    } catch (SerfCommunicationException e) {
      throw new IOException(e);
    }
  }

  public StreamSubscription memberLeaveStream() throws IOException {
    try {
      return client.stream("member-leave,member-failed");
    } catch (SerfCommunicationException e) {
      throw new IOException(e);
    }
  }

  public void handleMembershipEventStream(StreamSubscription streamSubscription, BiConsumer<String, Integer> addressConsumer) {
    executorService.execute(() -> {
      Thread.currentThread().setName("Serf Membership Event Handling Thread " + threadCounter.get());
      while (!Thread.interrupted()) {
        try {
          MembershipEvent membershipEvent = (MembershipEvent) streamSubscription.take();
          if (membershipEvent != null) {
            executorService.execute(() -> {
              try {
                handleMemberEvent(membershipEvent, addressConsumer);
              } catch (Exception e) {
                logger.warn("Unexpected Exception", e);
              }
            });
          }
        } catch (SerfCommunicationException e) {
          if ("Response handler has been stopped".equals(e.getMessage())) {
            Thread.currentThread().interrupt();
          } else {
            logger.error("Unexpected Serf Exception", e);
          }
        } catch (Exception e) {
          logger.error("Unexpected Exception", e);
        }
      }
      logger.debug("{} finished!", Thread.currentThread().getName());
    });
  }

  static String stripQuotes(String string) {
    return string.replaceAll("^\"|\"$", "");
  }

  void handleMemberEvent(MembershipEvent membershipEvent, BiConsumer<String, Integer> addressConsumer) {
    membershipEvent.getMembers().forEach(member -> {
      String host = member.getAddr().getHostAddress();
      String quotedPort = member.getTags().get("port");
      if (quotedPort == null) {
        logger.warn("Missing Tag for new Serf Member {}", member);
      } else {
        String portString = stripQuotes(quotedPort);
        try {
          Integer port = Integer.valueOf(portString);
          addressConsumer.accept(host, port);
        } catch (NumberFormatException e) {
          logger.warn("Malformed port tag {} for new Serf Member {}", portString, member);
        }
      }
    });
  }

  @Override
  public void close() {
    client.close();
    executorService.shutdownNow();
  }

}
