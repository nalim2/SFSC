package de.unistuttgart.isw.sfsc.core.hazelcast;

import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import java.util.function.BiConsumer;

class BackendEventConsumer implements MembershipListener {

  private final BiConsumer<String, Integer> memberAddedEventConsumer;
  private final BiConsumer<String, Integer> memberRemovedEventConsumer;

  BackendEventConsumer(BiConsumer<String, Integer> memberAddedEventConsumer,
      BiConsumer<String, Integer> memberRemovedEventConsumer) {
    this.memberAddedEventConsumer = memberAddedEventConsumer;
    this.memberRemovedEventConsumer = memberRemovedEventConsumer;
  }

  @Override
  public void memberAdded(MembershipEvent membershipEvent) {
    String host = HazelcastNode.getHost(membershipEvent);
    int port = HazelcastNode.getPort(membershipEvent);
    memberAddedEventConsumer.accept(host, port);
  }

  @Override
  public void memberRemoved(MembershipEvent membershipEvent) {
    String host = HazelcastNode.getHost(membershipEvent);
    int port = HazelcastNode.getPort(membershipEvent);
    memberRemovedEventConsumer.accept(host, port);
  }

  @Override
  public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) { }

}

