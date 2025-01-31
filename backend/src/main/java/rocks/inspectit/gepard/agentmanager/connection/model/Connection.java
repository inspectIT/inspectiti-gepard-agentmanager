/* (C) 2024 */
package rocks.inspectit.gepard.agentmanager.connection.model;

import java.time.Instant;
import lombok.*;
import rocks.inspectit.gepard.commons.model.agent.Agent;

/**
 * Represents a connected agent. It is an internal data structure and not exposed to the API. Acts
 * as Aggregate Root.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Connection {

  /** The registration time. * */
  private Instant registrationTime;

  /** The time of the last communication. */
  private Instant lastFetch;

  /** The status of the connection. */
  private ConnectionStatus connectionStatus;

  /** The agent which is connected. */
  private Agent agent;
}
