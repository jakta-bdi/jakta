package it.unibo.jakta.agents.bdi.distributed_broker

import io.ktor.server.application.*
import it.unibo.jakta.agents.bdi.distributed_broker.plugins.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureRouting()
    configureSockets()
}