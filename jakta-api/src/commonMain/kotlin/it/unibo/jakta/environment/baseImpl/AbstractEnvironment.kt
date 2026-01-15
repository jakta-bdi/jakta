package it.unibo.jakta.environment.baseImpl

import it.unibo.jakta.environment.AgentBody
import it.unibo.jakta.agent.EnvironmentAgent
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.event.Event

/**
 * Environment which listens for external events and is capable to forward them to agents.
 * @param Body The type of [AgentBody] used by agents in this environment.
 * @param agents The set of agents present in this environment.
 */
abstract class AbstractEnvironment<Body: AgentBody>(
    initialAgents: Set<EnvironmentAgent<Body>>,
) : Environment<Body> {

    private val agents: MutableSet<EnvironmentAgent<Body>> = initialAgents.toMutableSet()


    override fun sendEvent(event: Event.External, filterFunction: (Body) -> Boolean, source: Body?) {
        agents.forEach { agent ->
            if (
                filterFunction(agent.body) &&
                environmentFilter(event, agent.body, source) &&
                agent.body != source
            ) {
                agent.externalInbox.send(event)
            }
        }

    }

    /**
     * Custom filter function to determine if an event should be sent to a specific agent.
     * @param event The external event to be sent.
     * @param body The body of the agent being considered.
     * @param sender The body of the agent sending the event, if applicable.
     * @return True if the event should be sent to the agent, false otherwise.
     */
    abstract fun environmentFilter(event: Event.External, body: Body, sender: Body?): Boolean

}
