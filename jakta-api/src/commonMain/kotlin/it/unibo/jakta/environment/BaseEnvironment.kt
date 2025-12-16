package it.unibo.jakta.environment

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.event.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.SendChannel

/**
 * Environment which listens for external events and is capable to forward them to agents.
 * @param agentFilteringFunction filtering function which potentially selects a subset of agents that will receive the information.
 */
open class BaseEnvironment(
    initialAgents: Set<Agent<*, *>> = emptySet(),
    private val channel: Channel<Event.External> = Channel(UNLIMITED),
    val agentFilteringFunction: Agent<*, *>.() -> Boolean = { true },  // TODO(Maybe not enough)
) : Environment, SendChannel<Event.External> by channel  {

    private val _agents: MutableSet<Agent<*, *>> = initialAgents.toMutableSet()

    override val agents : Set<Agent<*, *>>
        get() = _agents.toSet()

    override fun addAgent(agent: Agent<*, *>) {
        _agents.add(agent)
    }

    override suspend fun processEvent() {
        // Inherits the coroutine scope from the outer scope
        val event = channel.receive()
        agents.filter { it.agentFilteringFunction() }.forEach { it.trySend(event) }
    }
}
