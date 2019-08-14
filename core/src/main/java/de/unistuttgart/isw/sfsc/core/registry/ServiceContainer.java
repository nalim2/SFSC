//package de.unistuttgart.isw.sfsc.core.registry;
//
//import java.util.Objects;
//import sfsc.AddressDescriptor;
//import sfsc.ServiceDescriptor;
//
//public class ServiceContainer {
//
//  private final ServiceDescriptor serviceDescriptor;
//
//  public ServiceContainer(ServiceDescriptor serviceDescriptor) {
//    this.serviceDescriptor = serviceDescriptor;
//  }
//
//  public ServiceDescriptor getServiceDescriptor() {
//    return serviceDescriptor;
//  }
//
//  @Override
//  public boolean equals(Object o) {
//    if (!(o instanceof ServiceContainer)) {
//      return false;
//    }
//    ServiceContainer that = (ServiceContainer) o;
//    AddressDescriptor thatAddress = that.getServiceDescriptor().getAddress();
//    AddressDescriptor thisAddress = serviceDescriptor.getAddress();
//    return new AddressDescriptorEquator().equate(thisAddress, thatAddress);
//  }
//
//  @Override
//  public int hashCode() {
//    return new AddressDescriptorEquator().hash(serviceDescriptor.getAddress());
//  }
//
//  private static class AddressDescriptorEquator {
//
//    int hash(AddressDescriptor addressDescriptor) {
//      return Objects.hash(addressDescriptor.getCoreId(), addressDescriptor.getAdapterId(), addressDescriptor.getServiceId());
//    }
//
//    boolean equate(AddressDescriptor addressDescriptor1, AddressDescriptor addressDescriptor2) {
//      return Objects.equals(addressDescriptor1.getCoreId(), addressDescriptor2.getCoreId())
//          && Objects.equals(addressDescriptor1.getAdapterId(), addressDescriptor2.getAdapterId())
//          && Objects.equals(addressDescriptor1.getServiceId(), addressDescriptor2.getServiceId());
//    }
//  }
//}
