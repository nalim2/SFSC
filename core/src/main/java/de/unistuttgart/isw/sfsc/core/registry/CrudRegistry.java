//package de.unistuttgart.isw.sfsc.core.registry;
//
//import java.util.Objects;
//import java.util.Set;
//import java.util.stream.Collectors;
//import sfsc.ServiceDescriptor;
//
//public class CrudRegistry {
//
//  private final RegistrySet<ServiceContainer> registrySet = new SimpleRegistrySet<>();
//
//  ServiceDescriptor create(ServiceDescriptor serviceDescriptor) {
//    if (!validate(serviceDescriptor)) {
//      throw new IllegalArgumentException();
//    } else {
//      registrySet.add(new ServiceContainer(serviceDescriptor));
//      return serviceDescriptor;
//    }
//  }
//
//  Set<ServiceDescriptor> read(ServiceDescriptor pattern) {
//    return registrySet
//        .getMatching(serviceContainer -> {
//              ServiceDescriptor entry = serviceContainer.getServiceDescriptor();
//              return (pattern.getName() == null || Objects.equals(pattern.getName(), entry.getName()))
//                  && (pattern.getType() == null || Objects.equals(pattern.getType(), entry.getType()))
//                  && (pattern.getAddress() == null ||
//                  (
//                      (pattern.getAddress().getCoreId() == null || Objects.equals(pattern.getAddress().getCoreId(), entry.getAddress().getCoreId()))
//                          && (pattern.getAddress().getAdapterId() == null || Objects.equals(pattern.getAddress().getCoreId(), entry.getAddress().getCoreId()))
//                          && (pattern.getAddress().getServiceId() == null || Objects.equals(pattern.getAddress().getCoreId(), entry.getAddress().getCoreId()))
//                  )
//              );
//            }
//        )
//        .stream()
//        .map(ServiceContainer::getServiceDescriptor)
//        .collect(Collectors.toSet());
//  }
//
//  void remove(ServiceDescriptor serviceDescriptor) {
//    registrySet.remove(new ServiceContainer(serviceDescriptor));
//  }
//
//  boolean validate(ServiceDescriptor serviceDescriptor) {
//    return serviceDescriptor.getName() != null
//        && serviceDescriptor.getType() != null
//        && serviceDescriptor.getAddress() != null
//        && serviceDescriptor.getAddress().getCoreId() != null
//        && serviceDescriptor.getAddress().getAdapterId() != null
//        && serviceDescriptor.getAddress().getServiceId() != null;
//  }
//}
