package servicepatterns.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.commonjava.util.synchronizing.Awaitable;
import de.unistuttgart.isw.sfsc.framework.descriptor.SfscServiceDescriptor;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import servicepatterns.basepatterns.ackreqrep.AckServerResult;

public interface SfscServiceApi {

  Set<SfscServiceDescriptor> getServices();

  Set<SfscServiceDescriptor> getServices(String name);

  Set<SfscServiceDescriptor> getServices(String name, Message message, Collection<String> varPaths);

  SfscServer server(SfscServerParameter parameter, Function<ByteString, AckServerResult> serverFunction);

  SfscClient client();

  SfscPublisher publisher(SfscPublisherParameter sfscPublisherParameter);

  SfscSubscriber subscriber(SfscServiceDescriptor publisherDescriptor, Consumer<ByteString> subscriberConsumer);

  SfscServer channelFactory(SfscChannelFactoryParameter parameter, Function<ByteString, SfscPublisher> channelFactory);

  Handle addRegistryStoreEventListener(Consumer<StoreEvent<SfscServiceDescriptor>> listener);

  Handle addOneShotRegistryStoreEventListener(Predicate<StoreEvent<SfscServiceDescriptor>> predicate, Runnable runnable);

  Awaitable addOneShotRegistryStoreEventListener(Predicate<StoreEvent<SfscServiceDescriptor>> predicate);

}
