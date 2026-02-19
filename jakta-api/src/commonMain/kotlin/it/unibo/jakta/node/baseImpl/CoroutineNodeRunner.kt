package it.unibo.jakta.node.baseImpl

import co.touchlab.kermit.Logger
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentLifecycle
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.agent.basImpl.BaseAgentLifecycle
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.NodeRunner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class CoroutineNodeRunner<N : Node<*, *>>
    : NodeRunner<N> {

    private val agents: MutableMap<AgentLifecycle<*, *, *>, Job> = mutableMapOf()

    private val _nodes: MutableSet<N> = mutableSetOf()

    override val nodes: Set<N>
        get() = _nodes.toSet()

    private val logger: Logger =  Logger(
        Logger.config,
        this.toString()
    )

    override suspend fun run(node: N) {
        _nodes += node
        supervisorScope {
            launch {
                while (true) {
                    when (val event = node.systemEvents.next()) {
                        is SystemEvent.AgentAddition<*, *, *> -> addAgent(node, event.executableAgent)
                        is SystemEvent.AgentRemoval -> removeAgent(event.id)
                        is SystemEvent.ShutDownNode -> stopNode(node)
                    }
                }
            }
        }
        logger.i("Node $node START")
    }

    private fun CoroutineScope.addAgent(node: N, agent: ExecutableAgent<*, *, *>) {
        val newAgent = BaseAgentLifecycle(agent)
        val newJob = launch {
            supervisorScope {
                while (true) {
                    newAgent.step(this)
                }
            }
        }
        agents[newAgent] = newJob

        // remove the agent from the environment if the agent stops for unexpected reasons
        newJob.invokeOnCompletion {
            when(it) {
                is CancellationException -> {} // intentional removal
                else -> {
                    node.removeAgent(agent.id)
                }
            }
        }
    }

    private fun removeAgent(id: AgentID) {
        val (agent,job) = agents.entries.find { (agent, _) -> agent.executableAgent.id == id } ?: return
        //TODO is it ok to cancel if it has been already stopped with exception?
        job.cancel(CancellationException("The agent has been removed from the MAS"))
        agents.remove(agent)
    }

    private fun CoroutineScope.stopNode(node: N){
        this.coroutineContext.job.cancel(CancellationException("ShutDownMAS requested"))
        //agents.forEach { removeAgent(it.key.executableAgent.id) }
        //return@launch
        _nodes -= node
        logger.i("Node $node has been stopped")
    }
}
