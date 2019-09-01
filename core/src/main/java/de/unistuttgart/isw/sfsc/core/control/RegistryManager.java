package de.unistuttgart.isw.sfsc.core.control;

import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol.PAYLOAD_FRAME;
import static de.unistuttgart.isw.sfsc.commonjava.protocol.pubsub.DataProtocol.TOPIC_FRAME;

import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.commonjava.zmq.processors.MessageDistributor.TopicListener;
import de.unistuttgart.isw.sfsc.commonjava.zmq.pubsubsocketpair.PubSubConnection.Publisher;
import de.unistuttgart.isw.sfsc.core.hazelcast.Registry;
import de.unistuttgart.isw.sfsc.protocol.registry.CreateRequest;
import de.unistuttgart.isw.sfsc.protocol.registry.CreateResponse;
import de.unistuttgart.isw.sfsc.protocol.registry.DeleteRequest;
import de.unistuttgart.isw.sfsc.protocol.registry.DeleteResponse;
import de.unistuttgart.isw.sfsc.protocol.registry.ReadResponse;
import de.unistuttgart.isw.sfsc.protocol.registry.RegistryMessage;
import de.unistuttgart.isw.sfsc.protocol.registry.ServiceDescriptor;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RegistryManager implements TopicListener {

  public static final String TOPIC = "de/unistuttgart/isw/sfsc/commonjava/registry";

  private static final Logger logger = LoggerFactory.getLogger(RegistryManager.class);

  private final Publisher publisher;
  private final Registry registry;

  RegistryManager(Publisher publisher, Registry registry) {
    this.publisher = publisher;
    this.registry = registry;
  }

  @Override
  public String getTopic(){
    return TOPIC;
  }

  @Override
  public boolean test(String topic) {
    return topic.startsWith(TOPIC);
  }

  @Override
  public void processMessage(byte[][] message) {
    try {
      RegistryMessage registryMessage = PAYLOAD_FRAME.get(message, RegistryMessage.parser());
      switch (registryMessage.getPayloadCase()) {
        case CREATE_REQUEST: {
          CreateRequest createRequest = PAYLOAD_FRAME.get(message, CreateRequest.parser());
          registry.create(createRequest.getService());
          String topic = new String(TOPIC_FRAME.get(message));
          RegistryMessage payload = RegistryMessage.newBuilder(registryMessage).setCreateResponse(CreateResponse.newBuilder().build()).build();
          publisher.publish(topic, payload);
          break;
        }
        case READ_REQUEST: {
          Set<ServiceDescriptor> services = registry.read();
          String topic = new String(TOPIC_FRAME.get(message)); //todo charset
          RegistryMessage payload = RegistryMessage.newBuilder(registryMessage)
              .setReadResponse(ReadResponse.newBuilder().addAllServices(services).build()).build();
          publisher.publish(topic, payload);
          break;
        }
        case DELETE_REQUEST: {
          DeleteRequest deleteRequest = PAYLOAD_FRAME.get(message, DeleteRequest.parser());
          registry.delete(deleteRequest.getService());
          String topic = new String(TOPIC_FRAME.get(message));
          RegistryMessage payload = RegistryMessage.newBuilder(registryMessage).setDeleteResponse(DeleteResponse.newBuilder().build()).build();
          publisher.publish(topic, payload);
          break;
        }
        default: {
          logger.warn("received registry message with currently unsupported type {}", registryMessage.getPayloadCase());
          break;
        }
      }
    } catch (InvalidProtocolBufferException e) {
      logger.warn("received malformed message", e);
    }
  }
}
