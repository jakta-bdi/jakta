package it.unibo.jakta.agents.bdi.distributed_broker

import io.ktor.server.application.Application
import it.unibo.jakta.agents.bdi.distributed_broker.plugins.configureRouting
import it.unibo.jakta.agents.bdi.distributed_broker.plugins.configureSockets

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureRouting()
    configureSockets()
}
