package de.unistuttgart.isw.sfsc.framework.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.synchronizing.Awaitable;
import de.unistuttgart.isw.sfsc.framework.api.services.channelfactory.ChannelFactoryResult;
import de.unistuttgart.isw.sfsc.framework.api.services.channelfactory.SfscChannelFactoryParameter;
import de.unistuttgart.isw.sfsc.framework.api.services.clientserver.SfscClient;
import de.unistuttgart.isw.sfsc.framework.api.services.clientserver.SfscServer;
import de.unistuttgart.isw.sfsc.framework.api.services.clientserver.SfscServerParameter;
import de.unistuttgart.isw.sfsc.framework.api.services.pubsub.SfscPublisher;
import de.unistuttgart.isw.sfsc.framework.api.services.pubsub.SfscPublisherParameter;
import de.unistuttgart.isw.sfsc.framework.api.services.pubsub.SfscSubscriber;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import de.unistuttgart.isw.sfsc.framework.patterns.ackreqrep.AckServerResult;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface SfscServiceApi extends NotThrowingAutoCloseable {

  Set<SfscServiceDescriptor> getServices();

  Set<SfscServiceDescriptor> getServices(String name);

  Set<SfscServiceDescriptor> getServices(String name, Message message, Collection<String> varPaths);

  SfscServer server(SfscServerParameter parameter, Function<ByteString, AckServerResult> serverFunction);

  SfscClient client();

  SfscPublisher publisher(SfscPublisherParameter sfscPublisherParameter);

  SfscSubscriber subscriber(SfscServiceDescriptor publisherDescriptor, Consumer<ByteString> subscriberConsumer);

  SfscServer channelFactory(SfscChannelFactoryParameter parameter, Function<ByteString, ChannelFactoryResult> channelFactory);

  Handle addRegistryStoreEventListener(Consumer<StoreEvent<SfscServiceDescriptor>> listener);

  Handle addOneShotRegistryStoreEventListener(Predicate<StoreEvent<SfscServiceDescriptor>> predicate, Runnable runnable);

  Awaitable addOneShotRegistryStoreEventListener(Predicate<StoreEvent<SfscServiceDescriptor>> predicate);

  Handle addCoreLostEventListener(Runnable runnable);

}
