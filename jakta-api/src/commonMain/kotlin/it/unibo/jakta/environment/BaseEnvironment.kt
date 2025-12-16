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
open class BaseEnvironment<Belief : Any, Goal : Any>(
    initialAgents: Set<Agent<Belief, Goal>>,
    private val channel: Channel<Event.External> = Channel(UNLIMITED),
    val agentFilteringFunction: Agent<Belief, Goal>.() -> Boolean = { true },  // TODO(Maybe not enough)
) : Environment<Belief, Goal>, SendChannel<Event.External> by channel  {

    private val _agents: MutableSet<Agent<Belief, Goal>> = initialAgents.toMutableSet()

    override val agents : Set<Agent<Belief, Goal>>
        get() = _agents.toSet()

    override fun addAgent(agent: Agent<Belief, Goal>) {
        _agents.add(agent)
    }

    override suspend fun processEvent() {
        // Inherits the coroutine scope from the outer scope
        val event = channel.receive()
        agents.filter { it.agentFilteringFunction() }.forEach { it.trySend(event) }
    }
}
