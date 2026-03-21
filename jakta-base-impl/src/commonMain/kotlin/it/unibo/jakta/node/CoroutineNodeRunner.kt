package it.unibo.jakta.node

import co.touchlab.kermit.Logger
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentLifecycle
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.agent.BaseAgentLifecycle
import it.unibo.jakta.event.SystemEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * A [it.unibo.jakta.node.NodeRunner] implementation that uses Kotlin coroutines to manage the execution of agents within a node.
 */
class CoroutineNodeRunner<Body: Any, Skills: Any, N : Node<Body, Skills>> : NodeRunner<N> {

    private val agents: MutableMap<AgentLifecycle<*, *, *>, Job> = mutableMapOf()

    private val _nodes: MutableSet<N> = mutableSetOf()

    override val nodes: Set<N>
        get() = _nodes.toSet()

    private val logger: Logger = Logger(
        Logger.config,
        this.toString(),
    )

    override suspend fun run(node: N) {
        supervisorScope {
            val appScope = this
            _nodes += node
            launch {
                while (isActive) {
                    when (val event = node.systemEvents.next()) {
                        is SystemEvent.AgentAddition<*, *, *> -> appScope.addAgent(node, event.executableAgent)
                        is SystemEvent.AgentRemoval -> removeAgent(event.id)
                        is SystemEvent.ShutDownNode -> appScope.stopNode(node)
                    }
                }
            }
            node.behaviors.forEach { launch {it.start(node)} }
        }
        logger.i("Node $node START")
    }

    private fun CoroutineScope.addAgent(node: N, agent: ExecutableAgent<*, *, *>) {
        val newAgent = BaseAgentLifecycle(agent)
        val newJob = launch {
            supervisorScope {
                while (isActive) {
                    newAgent.step(this)
                }
            }
        }
        agents += newAgent to newJob

        // remove the agent from the environment if the agent stops for unexpected reasons
        newJob.invokeOnCompletion {
            when (it) {
                is CancellationException -> {} // intentional removal
                else -> {
                    node.removeAgent(agent.id)
                }
            }
        }
    }

    private fun removeAgent(id: AgentID) {
        val (agent, job) = agents.entries.find { (agent, _) -> agent.executableAgent.id == id } ?: return
        // TODO is it ok to cancel if it has been already stopped with exception?
        job.cancel(CancellationException("The agent has been removed from the MAS"))
        agents.remove(agent)
    }

    private fun CoroutineScope.stopNode(node: N) {
        this.coroutineContext.cancel(CancellationException("Termination requested"))
        //TODO we are brutally killing the agents
        // agents.forEach { removeAgent(it.key.executableAgent.id) }
        // return@launch
        _nodes -= node
        logger.i("Node $node has been stopped")
    }
}
