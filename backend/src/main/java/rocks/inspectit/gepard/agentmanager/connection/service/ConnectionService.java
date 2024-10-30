/* (C) 2024 */
package rocks.inspectit.gepard.agentmanager.connection.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rocks.inspectit.gepard.agentmanager.agent.model.Agent;
import rocks.inspectit.gepard.agentmanager.connection.model.Connection;
import rocks.inspectit.gepard.agentmanager.connection.model.dto.ConnectionDto;
import rocks.inspectit.gepard.agentmanager.connection.model.dto.CreateConnectionRequest;
import rocks.inspectit.gepard.agentmanager.connection.model.dto.QueryConnectionRequest;
import rocks.inspectit.gepard.agentmanager.connection.validation.RegexQueryService;

/** Service-Implementation for handling agent connection requests. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionService {

  private final ConcurrentHashMap<UUID, Connection> connectionCache;

  private final RegexQueryService regexQueryService;

  /**
   * Handles a connection request from an agent.
   *
   * @param connectRequest The request for the new connection to be created.
   * @return Connection The response containing all saved information.
   */
  public Connection handleConnectRequest(CreateConnectionRequest connectRequest) {
    Connection connection = CreateConnectionRequest.toConnection(connectRequest);
    connectionCache.put(connection.getId(), connection);

    return connection;
  }

  /**
   * Returns all connections in the cache.
   *
   * @return List<ConnectionDto> All connections from the cache.
   */
  public List<ConnectionDto> getConnections() {
    return connectionCache.values().stream().map(ConnectionDto::fromConnection).toList();
  }

  /**
   * Query connections based on the query request.
   *
   * @param query The query request.
   * @return List<ConnectionDto> The connections that match the query.
   */
  public List<ConnectionDto> queryConnections(QueryConnectionRequest query) {
    return connectionCache.values().stream()
        .filter(connection -> matchesConnection(connection, query))
        .map(ConnectionDto::fromConnection)
        .collect(Collectors.toList());
  }

  /**
   * Returns a connection from the cache by its id.
   *
   * @param id The id of the connection.
   * @return ConnectionDto The connection.
   * @throws NoSuchElementException if no connection with the given id is found in the cache.
   */
  public ConnectionDto getConnection(UUID id) {
    if (!connectionCache.containsKey(id)) {
      throw new NoSuchElementException("No connection with id " + id + " found in cache.");
    }
    return ConnectionDto.fromConnection(connectionCache.get(id));
  }

  /**
   * Checks if a connection matches the given query.
   *
   * @param connection The connection to check.
   * @param query The query to match against.
   * @return true if the connection matches the query, false otherwise.
   */
  private boolean matchesConnection(Connection connection, QueryConnectionRequest query) {
    boolean matches = true;

    matches &= regexQueryService.matches(connection.getId().toString(), query.id());
    matches &=
        regexQueryService.matches(
            connection.getRegistrationTime().toString(), query.registrationTime());

    if (query.agent() != null) {

      QueryConnectionRequest.QueryAgentRequest queryAgent = query.agent();
      Agent agent = connection.getAgent();

      matches &= matchesAgent(agent, queryAgent);
    }

    return matches;
  }

  /**
   * Checks if an agent matches the given query.
   *
   * @param agent The agent to check.
   * @param query The query to match against.
   * @return true if the agent matches the query, false otherwise.
   */
  private boolean matchesAgent(Agent agent, QueryConnectionRequest.QueryAgentRequest query) {

    boolean matches = true;

    matches &= regexQueryService.matches(agent.getServiceName(), query.serviceName());
    matches &= regexQueryService.matchesLong(agent.getPid(), query.pid());
    matches &= regexQueryService.matches(agent.getGepardVersion(), query.gepardVersion());
    matches &= regexQueryService.matches(agent.getOtelVersion(), query.otelVersion());
    matches &= regexQueryService.matchesInstant(agent.getStartTime(), query.startTime());
    matches &= regexQueryService.matches(agent.getJavaVersion(), query.javaVersion());

    if (query.attributes() != null && !query.attributes().isEmpty()) {
      matches &= matchesAttributes(agent.getAttributes(), query.attributes());
    }
    return matches;
  }

  /**
   * Checks if a set of agent attributes matches the given query attributes.
   *
   * @param agentAttributes The agent attributes to check.
   * @param queryAttributes The query attributes to match against.
   * @return true if the agent attributes match the query attributes, false otherwise.
   */
  private boolean matchesAttributes(
      Map<String, String> agentAttributes, Map<String, String> queryAttributes) {
    return queryAttributes.entrySet().stream()
        .allMatch(
            queryEntry -> {
              String actualValue = agentAttributes.get(queryEntry.getKey());
              return actualValue != null
                  && regexQueryService.matches(actualValue, queryEntry.getValue());
            });
  }
}
