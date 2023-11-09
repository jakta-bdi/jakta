package it.unibo.jakta.agents.distributed.dmas

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.actions.effects.BroadcastMessage
import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.actions.effects.PopMessage
import it.unibo.jakta.agents.bdi.actions.effects.RemoveAgent
import it.unibo.jakta.agents.bdi.actions.effects.RemoveData
import it.unibo.jakta.agents.bdi.actions.effects.SendMessage
import it.unibo.jakta.agents.bdi.actions.effects.SpawnAgent
import it.unibo.jakta.agents.bdi.actions.effects.UpdateData
import it.unibo.jakta.agents.bdi.actions.effects.AddData
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.distributed.network.Network
import it.unibo.jakta.agents.distributed.remoteagent.RemoteAgent

/**
 * A DistributedMas is a Mas with networking capabilities: it can send and receive messages from other DMas' over the
 * network.
 */
internal class DMas(
    override val executionStrategy: ExecutionStrategy,
    override var environment: Environment,
    override var agents: Iterable<Agent>,
    private val remoteAgents: Iterable<RemoteAgent>,
    private val network: Network,
) : Mas {
    override fun start(debugEnabled: Boolean) {
        // Here the DMas should subscribe to the broker and start listening for messages
        TODO("Not yet implemented")
    }

    override fun applyEnvironmentEffects(effects: Iterable<EnvironmentChange>) = effects.forEach {
        when (it) {
            is BroadcastMessage -> {
                network.send(it)
                environment.broadcastMessage(it.message)
            }
            is RemoveAgent -> {
                agents = agents.filter { agent -> agent.name != it.agentName }
                executionStrategy.removeAgent(it.agentName)
                environment = environment.removeAgent(it.agentName)
            }
            is SendMessage -> {
                if (remoteAgents.map { rA -> rA.name }.contains(it.recipient)) {
                    network.send(it)
                    environment
                } else {
                    environment.submitMessage(it.recipient, it.message)
                }
            }
            is SpawnAgent -> {
                agents += it.agent
                executionStrategy.spawnAgent(it.agent)
                environment = environment.addAgent(it.agent)
            }
            is AddData -> environment = environment.addData(it.key, it.value)
            is RemoveData -> environment = environment.removeData(it.key)
            is UpdateData -> environment = environment.updateData(it.newData)
            is PopMessage -> environment = environment.popMessage(it.agentName)
        }
    }
}
