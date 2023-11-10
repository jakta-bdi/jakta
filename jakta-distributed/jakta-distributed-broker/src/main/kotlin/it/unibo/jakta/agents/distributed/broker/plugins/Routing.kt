package it.unibo.jakta.agents.distributed.broker.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import it.unibo.jakta.agents.distributed.broker.model.SubscriptionManager

fun Application.configureRouting(subscriptionManager: SubscriptionManager) {
    install(ContentNegotiation) {
        json()
    }
    routing {
        route("/topics") {
            get {
                call.respond(subscriptionManager.availableTopics())
            }
        }

        route("/uniqueID") {
            get {
                call.respond(subscriptionManager.generateUniqueID())
            }
        }
    }
}
