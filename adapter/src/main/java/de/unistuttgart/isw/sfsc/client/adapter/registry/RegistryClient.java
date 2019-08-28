package de.unistuttgart.isw.sfsc.client.adapter.registry;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public interface RegistryClient {
  String REGISTRY_TOPIC = "registry";

   ServiceHandle addService(ServiceDeclaration serviceDeclaration) throws ExecutionException, InterruptedException;

   Set<ServiceHandle> getServices() throws ExecutionException, InterruptedException;

   void removeService(ServiceHandle serviceHandle) throws ExecutionException, InterruptedException;

}
