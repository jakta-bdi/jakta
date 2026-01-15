package it.unibo.jakta.environment

import it.unibo.jakta.agent.EnvironmentAgent
import it.unibo.jakta.environment.baseImpl.AbstractEnvironment
import it.unibo.jakta.event.Event

/**
 * Represents the shared environment in which the agents operate.
 * @param Body The type of [AgentBody] used by agents in this environment.
 */
interface Environment<Body: AgentBody> {

    /**
     * Sends an external [event] to all agents in the environment that satisfy the [filterFunction].
     * Optionally, a [source] body can be specified if the event originates from a specific agent (e.g. sending a message).
     * @param event The external event to be sent.
     * @param filterFunction A function that determines the conditions under which an agent should receive the event.
     * @param source The body of the agent sending the event, if applicable.
     */
    fun sendEvent(event: Event.External,
                  filterFunction: (Body) -> Boolean = { true },
                  source: Body?
    )
}
