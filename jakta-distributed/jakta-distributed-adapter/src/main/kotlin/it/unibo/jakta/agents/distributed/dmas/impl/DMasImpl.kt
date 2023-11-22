package it.unibo.jakta.agents.distributed.dmas.impl

import it.unibo.jakta.agents.bdi.Agent
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
import it.unibo.jakta.agents.distributed.RemoteService
import it.unibo.jakta.agents.distributed.dmas.DMas
import it.unibo.jakta.agents.distributed.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class DMasImpl(
    override val executionStrategy: ExecutionStrategy,
    override var environment: Environment,
    override var agents: Iterable<Agent>,
    override val services: Iterable<RemoteService>,
    override val network: Network,
) : DMas {

    init {
        agents.forEach { environment = environment.addAgent(it) }
    }

    override fun start(debugEnabled: Boolean) {
        val self = this
        runBlocking {
            // Here the DMas should subscribe to the broker and start listening for messages
            launch(Dispatchers.Default) {
                services.forEach { service ->
                    network.subscribe(service)
                }
            }
            executionStrategy.dispatch(self, debugEnabled)
        }
    }

    override fun applyEnvironmentEffects(effects: Iterable<EnvironmentChange>) {
        val externalEffects = network.getMessagesAsEnvironmentChanges()
        (effects + externalEffects).forEach { environmentChange ->
            when (environmentChange) {
                is BroadcastMessage -> {
                    environment = environment.broadcastMessage(environmentChange.message)
                }

                is RemoveAgent -> {
                    agents = agents.filter { agent -> agent.name != environmentChange.agentName }
                    executionStrategy.removeAgent(environmentChange.agentName)
                    environment = environment.removeAgent(environmentChange.agentName)
                }

                is SendMessage -> {
                    if (services.map { it.serviceName }.contains(environmentChange.recipient)) {
                        runBlocking {
                            launch {
                                network.send(environmentChange)
                            }
                        }
                    } else {
                        environment = environment.submitMessage(environmentChange.recipient, environmentChange.message)
                    }
                }

                is SpawnAgent -> {
                    agents += environmentChange.agent
                    executionStrategy.spawnAgent(environmentChange.agent)
                    environment = environment.addAgent(environmentChange.agent)
                }

                is AddData -> environment = environment.addData(environmentChange.key, environmentChange.value)
                is RemoveData -> environment = environment.removeData(environmentChange.key)
                is UpdateData -> environment = environment.updateData(environmentChange.newData)
                is PopMessage -> environment = environment.popMessage(environmentChange.agentName)
            }
        }
    }
}
