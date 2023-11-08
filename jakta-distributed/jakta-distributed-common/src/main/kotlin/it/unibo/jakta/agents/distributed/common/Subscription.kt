package it.unibo.jakta.agents.distributed.common

import kotlinx.serialization.Serializable

@Serializable
data class Subscription(val topics: Set<Topic>)
