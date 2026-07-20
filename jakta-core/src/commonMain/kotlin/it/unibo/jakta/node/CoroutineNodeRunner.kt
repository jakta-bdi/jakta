package it.unibo.jakta.node

import co.touchlab.kermit.Logger
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentLifecycle
import it.unibo.jakta.agent.BaseAgentLifecycle
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.event.SystemEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.yield

/**
 * A [NodeRunner] implementation that uses Kotlin coroutines
 * to manage the execution of agents within a node.
 * @param [connection] The [NodeConnection] used for communication and event handling.
 * @param [Body] The type of the agent's body.
 * @param [N] The type of the executable node that this runner will manage.
 */
class CoroutineNodeRunner<Body : Any, N : ExecutableNode<Body>>(val connection: NodeConnection) : NodeRunner<N> {

    private val agents: MutableMap<AgentLifecycle<*, *>, Job> = mutableMapOf()

    private val _nodes: MutableSet<N> = mutableSetOf()

    override val nodes: Set<N>
        get() = _nodes.toSet()

    private val logger: Logger = Logger(
        Logger.config,
        this.toString(),
    )

    override suspend fun run(node: N) {
        logger.i("Node $node started")
        supervisorScope {
            val appScope = this
            _nodes += node
            val subscription = connection.subscribe()

            // propagate local events on the remote connection
            launch {
                while (isActive) {
                    connection.send(node.systemEvents.next())
                }
            }

            // handle incoming events from the remote connection
            launch {
                while (isActive) {
                    val event = subscription.queue.next()
                    node.handleExternalEvent(event)
                    when (event) {
                        is SystemEvent.AgentAddition<*, *> -> appScope.startAgent(node, event.executableAgent)

                        is SystemEvent.AgentRemoval -> stopAgent(event.id)

                        is SystemEvent.ShutDownNode -> if (event.nodeID == node.id) {
                            stopNode(node, subscription, appScope.coroutineContext.job)
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun CoroutineScope.startAgent(node: N, agent: ExecutableAgent<*, *>) {
        val newAgent = BaseAgentLifecycle(agent)
        val newJob = launch {
            while (isActive) {
                newAgent.step()
                yield()
            }
        }
        agents += newAgent to newJob

        // remove the agent from the environment if the agent stops for unexpected reasons
        newJob.invokeOnCompletion {
            when (it) {
                is CancellationException -> {}

                // intentional removal
//                is IllegalArgumentException -> {
//                    this.stopNode(node)
//                    throw it
//                }
                else -> {
                    logger.e { "Agent ${agent.id} has stopped unexpectedly with cause: $it" }
                    node.removeAgent(agent.id)
                }
            }
        }
    }

    private fun stopAgent(id: AgentID) {
        val (agent, job) = agents.entries.find { (agent, _) -> agent.executableAgent.id == id } ?: return
        // TODO is it ok to cancel if it has been already stopped with exception?
        job.cancel(CancellationException("The agent has been removed from the MAS"))
        agents.remove(agent)
    }

    private suspend fun stopNode(node: N, subscription: NodeSubscription, parentJob: Job) {
        subscription.close()
        _nodes -= node
        logger.i("Node $node has been stopped")
        parentJob.children.forEach({ it.cancel(CancellationException("Termination requested")) })
    }
}
