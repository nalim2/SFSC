//package de.unistuttgart.isw.sfsc.core.registry;
//
//
//import com.google.protobuf.Message;
//import java.util.Set;
//import java.util.concurrent.ExecutorService;
//import org.zeromq.ZMQ.Socket;
//import sfsc.AddressDescriptor;
//import sfsc.CreateRequest;
//import sfsc.CreateResponse;
//import sfsc.DeleteRequest;
//import sfsc.DeleteResponse;
//import sfsc.ReadRequest;
//import sfsc.ReadResponse;
//import sfsc.ServiceDescriptor;
//import sfsc.ZmqRegistryHeader;
//
//public class ZmqApi implements Runnable {
//
//  private final CrudRegistry de.unistuttgart.isw.sfsc.core.registry;
//  private final Socket socket;
//
//  public ZmqApi(CrudRegistry de.unistuttgart.isw.sfsc.core.registry, Socket socket) {
//    this.de.unistuttgart.isw.sfsc.core.registry = de.unistuttgart.isw.sfsc.core.registry;
//    this.socket = socket;
//  }
//
//  public void run() {
//    while (!Thread.interrupted()) {
//      byte[] origin = socket.recv();
//      byte[] headerBytes = socket.recv();
//      byte[] messageBytes = socket.recv();
//
//      ZmqRegistryHeader requestHeader = ZmqRegistryHeader.parseFrom(headerBytes);
//      Type controlType = requestHeader.getType();
//      Message response;
//      switch (controlType) {
//        case CREATE_REQUEST:
//          CreateRequest createRequest = CreateRequest.parseFrom(messageBytes);
//          ServiceDescriptor request = createRequest.getService();
//          ServiceDescriptor completed = complete(request);
//          ServiceDescriptor added = de.unistuttgart.isw.sfsc.core.registry.create(completed);
//          response = CreateResponse.newBuilder().setService(added).build();
//          break;
//        case READ_REQUEST:
//          ReadRequest readRequest = ReadRequest.parseFrom(messageBytes);
//          Set<ServiceDescriptor> matches = de.unistuttgart.isw.sfsc.core.registry.read(readRequest.getPattern());
//          response = ReadResponse.newBuilder().addAllServices(matches).build();
//          break;
//        case DELETE_REQUEST:
//          DeleteRequest deleteRequest = DeleteRequest.parseFrom(messageBytes);
//          de.unistuttgart.isw.sfsc.core.registry.remove(deleteRequest.getService());
//          response = DeleteResponse.newBuilder().build();
//          break;
//        default:
//          throw new UnsupportedOperationException("Received unsupported protocol.control type " + controlType);
//      }
//      socket.sendMore(origin);
//      socket.sendMore(buildResponseHeader(requestHeader).toByteArray());
//      socket.send(response.toByteArray());
//    }
//  }
//
//  ServiceDescriptor complete(ServiceDescriptor incomplete, String coreId, String adapterId, String serviceId) {
//    return ServiceDescriptor.newBuilder(incomplete)
//        .setAddress(
//            AddressDescriptor.newBuilder()
//                .setCoreId(coreId)
//                .setAdapterId(adapterId)
//                .setServiceId(serviceId)
//                .build()).build();
//  }
//
//  ZmqRegistryHeader buildResponseHeader(ZmqRegistryHeader requestHeader) {
//    int id = requestHeader.getId();
//    Type requestType = requestHeader.getType();
//    Type responseType;
//
//    switch (requestType) {
//      case CREATE_REQUEST:
//        responseType = CREATE_RESPONSE;
//        break;
//      case READ_REQUEST:
//        responseType = READ_RESPONSE;
//        break;
//      case DELETE_REQUEST:
//        responseType = DELETE_RESPONSE;
//        break;
//      default:
//        throw new IllegalStateException("Tried to build response header for unsupported request type: " + requestType);
//    }
//    return ZmqRegistryHeader.newBuilder().setId(id).setType(responseType).build();
//  }
//}
