package it.unibo.jakta.agents.distributed.dmas.impl

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.distributed.RemoteService
import it.unibo.jakta.agents.distributed.broker.embedded.EmbeddedBroker
import it.unibo.jakta.agents.distributed.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * This implementation has an embedded Broker for communication between DMas'.
 * This implementation is useful when running a cluster of Mas on the same machine.
 */
internal class DMasWithEmbeddedBroker(
    override var environment: Environment,
    override var agents: Iterable<Agent>,
    override val executionStrategy: ExecutionStrategy,
    override val network: Network,
    override val services: Iterable<RemoteService>,
    private val broker: EmbeddedBroker,
) : AbstractDMas(
    executionStrategy,
    environment,
    agents,
    services,
    network,
) {

    init {
        agents.forEach { environment = environment.addAgent(it) }
    }

    override fun start(debugEnabled: Boolean) {
        val self = this

        runBlocking {
            launch(Dispatchers.Default) { startEmbeddedBroker() }
            delay(3000)
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

    private fun startEmbeddedBroker() {
        try {
            broker.start()
        } catch (_: Exception) { }
    }
}
