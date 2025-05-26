package it.unibo.jakta.impl

import it.unibo.jakta.ASAgent
import it.unibo.jakta.Mas
import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.executionstrategies.ExecutionStrategy

internal class MasImpl(
    override val executionStrategy: ExecutionStrategy,
    override var environment: BasicEnvironment,
    override var agents: Iterable<ASAgent>,
) : Mas {

    override fun start(debugEnabled: Boolean) = executionStrategy.dispatch(this, debugEnabled)

    override fun applyEnvironmentEffects(effects: Iterable<EnvironmentChange>) = effects.forEach {
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
