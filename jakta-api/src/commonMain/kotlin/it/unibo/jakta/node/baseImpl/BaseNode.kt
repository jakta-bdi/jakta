package it.unibo.jakta.node.baseImpl

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentLifecycle
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.agent.basImpl.BaseAgentLifecycle
import it.unibo.jakta.environment.AgentBody
import it.unibo.jakta.environment.Runtime
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.node.Node
import kotlin.collections.component1
import kotlin.collections.component2
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class BaseNode<Body: AgentBody, Position: Any, R : Runtime<Position,*,Body>> (
    private val runtime: R,
    private val agentSpecifications: Map<AgentSpecification<*, *, *, Body>, Position>
) : Node {

    private val agents: MutableMap<AgentLifecycle<*, *, *>, Job> = mutableMapOf()

    override suspend fun run() = supervisorScope {
        launch {
            while (true) {
                when (val event = runtime.systemEvents.next()) {
                    is SystemEvent.AgentAddition<*, *, *> -> addAgent(event.executableAgent)
                    is SystemEvent.AgentRemoval -> removeAgent(event.id)
                    is SystemEvent.ShutDownMAS -> {
                        this.coroutineContext.job.cancel(CancellationException("ShutDownMAS requested"))
                        //agents.forEach { removeAgent(it.key.executableAgent.id) }
                        //return@launch
                    }
                }
            }
        }

        agentSpecifications.forEach{(a, p) -> runtime.addAgent(a, p)}
    }

    private fun CoroutineScope.addAgent(agent: ExecutableAgent<*,*,*>) {
        val newAgent = BaseAgentLifecycle(agent)
        val newJob = launch {
            supervisorScope {
                while(true) {
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
                    runtime.removeAgent(agent.id)
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
}
