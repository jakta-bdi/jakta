package it.unibo.jakta.agents.distributed.common

import kotlinx.serialization.Serializable

@Serializable
enum class Error {
    BAD_REQUEST,
    CLIENT_DISCONNECTED,
}
