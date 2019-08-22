package de.unistuttgart.isw.sfsc.core.hazelcast;

import static de.unistuttgart.isw.sfsc.core.hazelcast.HazelcastNode.GET_HOST;
import static de.unistuttgart.isw.sfsc.core.hazelcast.HazelcastNode.GET_PORT;

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
    String host = GET_HOST.apply(membershipEvent);
    int port = GET_PORT.apply(membershipEvent);
    memberAddedEventConsumer.accept(host, port);
  }

  @Override
  public void memberRemoved(MembershipEvent membershipEvent) {
    String host = GET_HOST.apply(membershipEvent);
    int port = GET_PORT.apply(membershipEvent);
    memberRemovedEventConsumer.accept(host, port);
  }

  @Override
  public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
  }

}

