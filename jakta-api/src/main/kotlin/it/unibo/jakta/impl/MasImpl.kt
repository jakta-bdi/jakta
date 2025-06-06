package it.unibo.jakta.impl

import it.unibo.jakta.Agent
import it.unibo.jakta.AgentProcess
import it.unibo.jakta.Mas
import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.executionstrategies.ExecutionStrategy
import kotlin.collections.forEach

internal class MasImpl<Belief : Any, Query : Any, Response>(
    override val executionStrategy: ExecutionStrategy<Belief, Query, Response>,
    override var environment: AgentProcess<Belief>,
    override var agents: Iterable<Agent<Belief, Query, Response>>,
) : Mas<Belief, Query, Response> {

    override fun start(debugEnabled: Boolean) = executionStrategy.dispatch(this, debugEnabled)

    override fun applyEnvironmentEffects(effects: Iterable<EnvironmentChange<Belief, Query, Response>>) = effects.forEach {
//        when (it) {
//            is BroadcastMessage -> environment = environment.broadcastMessage(it.message)
//            is RemoveAgent -> {
//                agents = agents.filter { agent -> agent.name != it.agentName }
//                executionStrategy.removeAgent(it.agentName)
//                environment = environment.removeAgent(it.agentName)
//            }
//            is SendMessage -> environment = environment.submitMessage(it.recipient, it.message)
//            is SpawnAgent -> {
//                agents += it.agent
//                executionStrategy.spawnAgent(it.agent)
//                environment = environment.addAgent(it.agent)
//            }
//            is AddData -> environment = environment.addData(it.key, it.value)
//            is RemoveData -> environment = environment.removeData(it.key)
//            is UpdateData -> environment = environment.updateData(it.newData)
//            is PopMessage -> environment = environment.popMessage(it.agentName)
//        }
        TODO()
    }
}
