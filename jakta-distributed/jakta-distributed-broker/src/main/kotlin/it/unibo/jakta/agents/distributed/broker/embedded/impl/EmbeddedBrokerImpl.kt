package it.unibo.jakta.agents.distributed.broker.embedded.impl

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import it.unibo.jakta.agents.distributed.broker.embedded.EmbeddedBroker
import it.unibo.jakta.agents.distributed.broker.module

class EmbeddedBrokerImpl(port: Int) : EmbeddedBroker {

    private val server: NettyApplicationEngine = embeddedServer(Netty, port = port, module = Application::module)
    override fun start() {
        server.start(wait = true)
    }
}
