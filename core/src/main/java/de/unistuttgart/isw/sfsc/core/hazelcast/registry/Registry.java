package de.unistuttgart.isw.sfsc.core.hazelcast.registry;

import com.hazelcast.replicatedmap.ReplicatedMap;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.command.CommandReply;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.command.CommandRequest;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.query.QueryReply;
import de.unistuttgart.isw.sfsc.clientserver.protocol.registry.query.QueryRequest;
import de.unistuttgart.isw.sfsc.commonjava.util.Handle;
import de.unistuttgart.isw.sfsc.commonjava.util.NotThrowingAutoCloseable;
import de.unistuttgart.isw.sfsc.core.hazelcast.registry.log.RegistryEventLog;
import de.unistuttgart.isw.sfsc.core.hazelcast.registry.replicatedregistry.ReplicatedRegistry;
import de.unistuttgart.isw.sfsc.serverserver.registry.RegistryEntry;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Registry implements NotThrowingAutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(Registry.class);
  private static final int REMOVE_EVENT_RETENTION_TIME_SEC = 120;

  private final ReplicatedRegistry replicatedRegistry; //for some reason, ReplicatedMap does a nn check on value. Void -> Boolean
  private final RegistryEventLog registryEventLog;
  private final String coreId;
  private final Handle handle;

  public Registry(ReplicatedMap<RegistryEntry, Boolean> replicatedMap, String coreId) {
    this.coreId = coreId;
    replicatedRegistry = new ReplicatedRegistry(replicatedMap);
    registryEventLog = new RegistryEventLog(REMOVE_EVENT_RETENTION_TIME_SEC);
    handle = replicatedRegistry.addListener(registryEventLog::onStoreEvent);
  }

  public Handle addEventListener(Consumer<QueryReply> listener) {
    return registryEventLog.addListener(listener);
  }

  public CommandReply handleCommand(CommandRequest commandRequest) {
    String adapterId = commandRequest.getAdapterId();
    switch (commandRequest.getCreateOrDeleteCase()) {
      case CREATE: {
        replicatedRegistry.add(RegistryEntry.newBuilder().setAdapterId(adapterId).setCoreId(coreId).setData(commandRequest.getCreate()).build());
        break;
      }
      case DELETE: {
        replicatedRegistry.remove(RegistryEntry.newBuilder().setAdapterId(adapterId).setCoreId(coreId).setData(commandRequest.getDelete()).build());
        break;
      }
      default: {
        logger.warn("Unsupported Store Event!");
        break;
      }
    }
    return CommandReply.getDefaultInstance();
  }

  public QueryReply handleQuery(QueryRequest queryRequest) {
    return registryEventLog.handleQueryRequest(queryRequest);
  }

  public void deleteEntries(String adapterId) {
    replicatedRegistry.removeAll(entry -> entry.getAdapterId().equals(adapterId));
  }

  @Override
  public void close() {
    handle.close();
    registryEventLog.close();
  }


}
