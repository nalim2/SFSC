package de.unistuttgart.isw.sfsc.client.adapter.registry;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface RegistryClient {

  Future<ServiceHandle> addService(ServiceDeclaration serviceDeclaration) throws ExecutionException, InterruptedException;

  Future<Set<ServiceHandle>> getServices() throws ExecutionException, InterruptedException;

  Future<Void> removeService(ServiceHandle serviceHandle) throws ExecutionException, InterruptedException;

}
