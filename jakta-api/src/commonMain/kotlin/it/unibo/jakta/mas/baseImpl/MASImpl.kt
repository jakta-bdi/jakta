package it.unibo.jakta.mas.baseImpl

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentLifecycle
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.RunnableAgent
import it.unibo.jakta.agent.basImpl.BaseAgentLifecycle
import it.unibo.jakta.environment.AgentBody
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.mas.MAS
import kotlin.collections.component1
import kotlin.collections.component2
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class BaseMAS<Body: AgentBody, Position: Any, E : Environment<Position,*,Body>> (
    private val environment: E,
    private val agentSpecifications: Map<AgentSpecification<*, *, *, Body>, Position>
) : MAS {

    private val agents: MutableMap<AgentLifecycle<*, *, *>, Job> = mutableMapOf()

    override suspend fun run() = supervisorScope {
        launch {
            while (true) {
                when (val event = environment.systemEvents.next()) {
                    is SystemEvent.AgentAddition<*, *, *> -> addAgent(event.runnableAgent)
                    is SystemEvent.AgentRemoval -> removeAgent(event.id)
                    is SystemEvent.ShutDownMAS -> break
                }
            }
        }

        agentSpecifications.forEach{(a, p) -> environment.addAgent(a, p)}
    }

    private fun CoroutineScope.addAgent(agent: RunnableAgent<*,*,*>) {
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
                    environment.removeAgent(agent.id)
                }
            }
        }
    }

    private fun removeAgent(id: AgentID) {
        val (agent,job) = agents.entries.find { (agent, _) -> agent.runnableAgent.id == id } ?: return
        //TODO is it ok to cancel if it has been already stopped with exception?
        job.cancel(CancellationException("The agent has been removed from the MAS"))
        agents.remove(agent)
    }
}
