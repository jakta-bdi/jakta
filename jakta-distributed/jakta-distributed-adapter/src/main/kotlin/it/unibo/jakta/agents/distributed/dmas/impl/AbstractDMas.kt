package it.unibo.jakta.agents.distributed.dmas.impl

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.actions.effects.AddData
import it.unibo.jakta.agents.bdi.actions.effects.BroadcastMessage
import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.actions.effects.PopMessage
import it.unibo.jakta.agents.bdi.actions.effects.RemoveAgent
import it.unibo.jakta.agents.bdi.actions.effects.RemoveData
import it.unibo.jakta.agents.bdi.actions.effects.SendMessage
import it.unibo.jakta.agents.bdi.actions.effects.SpawnAgent
import it.unibo.jakta.agents.bdi.actions.effects.UpdateData
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.jakta.agents.distributed.RemoteService
import it.unibo.jakta.agents.distributed.dmas.DMas
import it.unibo.jakta.agents.distributed.network.Network
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.parsing.parse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

abstract class AbstractDMas(
    override val executionStrategy: ExecutionStrategy,
    override var environment: Environment,
    override var agents: Iterable<Agent>,
    override val services: Iterable<RemoteService>,
    override val network: Network,
) : DMas {

    override fun start(debugEnabled: Boolean) {
        val self = this
        runBlocking {
            // Here the DMas should subscribe to the broker and start listening for messages
            services.map { service ->
                launch(Dispatchers.Default) {
                    network.subscribe(service)
                }
            }
            launch(Dispatchers.Default) {
                network.subscribe(RemoteService("broadcast"))
            }
            executionStrategy.dispatch(self, debugEnabled)
        }
    }

    override fun applyEnvironmentEffects(effects: Iterable<EnvironmentChange>) {
        // Create a list of SendMessages for every agent for each disconnected remote service
        val disconnectedAgents: Iterable<EnvironmentChange> =
            network.getDisconnections().toList().flatMap { disconnectedService ->
                agents.map { agent ->
                    SendMessage(
                        Message(
                            "broker",
                            Tell,
                            Struct.parse("disconnected(${disconnectedService.serviceName})"),
                        ),
                        agent.name,
                    )
                }
            }
        val externalEffects = network.getMessagesAsEnvironmentChanges()
        // filter out every EnvironmentChange that is a SendMessage that is not addressed to an agent in the DMas
        val effectsFiltered =
            externalEffects.filter { environmentChange ->
                environmentChange is SendMessage && agents.map { agent -> agent.name }
                    .contains(environmentChange.recipient) || environmentChange is BroadcastMessage
            }
        val changes = effects + effectsFiltered + disconnectedAgents
        changes.forEach { environmentChange ->
            when (environmentChange) {
                is BroadcastMessage -> {
                    // Prevent the DMas from broadcasting another time the BroadcastMessage
                    if (agents.map { it.name }.contains(environmentChange.message.from)) {
                        runBlocking {
                            launch(Dispatchers.Default) {
                                network.broadcast(environmentChange)
                            }
                        }
                    }
                    environment = environment.broadcastMessage(environmentChange.message)
                }

                is RemoveAgent -> {
                    agents = agents.filter { agent -> agent.name != environmentChange.agentName }
                    executionStrategy.removeAgent(environmentChange.agentName)
                    environment = environment.removeAgent(environmentChange.agentName)
                }

                is SendMessage -> {
                    println(services)
                    if (!agents.map { it.name }.contains(environmentChange.recipient)) {
                        runBlocking {
                            launch(Dispatchers.Default) {
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
