package it.unibo.jakta.agents.distributed.broker.embedded

import it.unibo.jakta.agents.distributed.broker.embedded.impl.EmbeddedBrokerImpl

interface EmbeddedBroker {
    fun start()

    companion object {
        operator fun invoke(port: Int = 8080): EmbeddedBroker = EmbeddedBrokerImpl(port)
    }
}
