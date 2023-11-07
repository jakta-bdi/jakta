package it.unibo.jakta.agents.distributed.broker

import io.ktor.server.application.Application
import it.unibo.jakta.agents.distributed.broker.plugins.configureSockets

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureSockets()
}
