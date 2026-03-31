package it.unibo.jakta.node

import it.unibo.jakta.agent.AgentLifecycle
import it.unibo.jakta.agent.BaseAgentLifecycle
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.event.SystemEvent
import kotlinx.coroutines.CoroutineDispatcher

/**
 * A NodeRunner where agent stepping is performed manually, one step at a time.
 * Includes a method to step until no more events are available.
 */
class ManualStepNodeRunner<Body: Any, Skills: Any, N : Node<Body, Skills>> : NodeRunner<N> {

    private val agents: MutableMap<AgentLifecycle<*, *, *>, Unit> = mutableMapOf()
    private val _nodes: MutableSet<N> = mutableSetOf()
    override val nodes: Set<N> get() = _nodes.toSet()

    /**
     * Adds a node to the runner. Processes immediate system events once.
     */
    override suspend fun run(node: N) {
        _nodes += node
        processSystemEvents(node)
    }

    /**
     * Steps all agents once on the provided dispatcher.
     */
    fun stepAll(dispatcher: CoroutineDispatcher) {
        agents.keys.forEach { it.tryStep(dispatcher) }
    }

    /**
     * Steps a single agent by ID.
     */
    fun stepAgent(id: AgentID, dispatcher: CoroutineDispatcher) {
        agents.keys.find { it.executableAgent.id == id }?.tryStep(dispatcher)
    }

    /**
     * Steps all agents repeatedly until no events remain in the node or agents' inboxes.
     */
    fun stepUntilIdle(dispatcher: CoroutineDispatcher) {
        var hasEvents: Boolean
        do {
            hasEvents = false
            // Step all agents once
            agents.keys.forEach {
                it.tryStep(dispatcher)
                if (it.executableAgent.events.tryNext() != null) hasEvents = true
            }
            // Check system events for each node
            _nodes.forEach { node ->
                while (true) {
                    val event = node.systemEvents.tryNext() ?: break
                    hasEvents = true
                    when (event) {
                        is SystemEvent.AgentAddition<*, *, *> -> addAgent(event.executableAgent)
                        is SystemEvent.AgentRemoval -> removeAgent(event.id)
                        is SystemEvent.ShutDownNode -> _nodes -= node
                    }
                }
            }
        } while (hasEvents)
    }

    /**
     * Adds an agent manually.
     */
    fun addAgent(agent: ExecutableAgent<*, *, *>) {
        val lifecycle = BaseAgentLifecycle(agent)
        agents[lifecycle] = Unit
    }

    /**
     * Removes an agent manually.
     */
    fun removeAgent(id: AgentID) {
        val entry = agents.keys.find { it.executableAgent.id == id } ?: return
        agents.remove(entry)
    }

    /**
     * Processes immediate system events once.
     */
    private fun processSystemEvents(node: N) {
        while (true) {
            val event = node.systemEvents.tryNext() ?: break
            when (event) {
                is SystemEvent.AgentAddition<*, *, *> -> addAgent(event.executableAgent)
                is SystemEvent.AgentRemoval -> removeAgent(event.id)
                is SystemEvent.ShutDownNode -> _nodes -= node
            }
        }
    }
}
