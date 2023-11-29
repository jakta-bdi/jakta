package it.unibo.jakta.agents.distributed.broker.model

import kotlinx.serialization.Serializable

@Serializable
enum class Error(val code: Int) {
    BAD_REQUEST(400),
    CLIENT_DISCONNECTED(499),
}
