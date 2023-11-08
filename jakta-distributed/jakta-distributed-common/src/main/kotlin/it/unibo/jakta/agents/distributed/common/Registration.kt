package it.unibo.jakta.agents.distributed.common

import kotlinx.serialization.Serializable

@Serializable
data class Registration(val token: MasID)
