package it.unibo.jakta.agents.distributed.common

import kotlinx.serialization.Serializable

@Serializable
data class Update(val topic: Topic, val message: SerializableMessage)
