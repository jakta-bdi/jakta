package it.unibo.jakta.agents.distributed.broker.model

import kotlinx.serialization.Serializable
import java.util.concurrent.atomic.AtomicInteger

@Serializable
data class MasID(val id: String = "Mas${UniqueMasIDGenerator.lastId.getAndIncrement()}")

internal object UniqueMasIDGenerator {
    val lastId = AtomicInteger(0)
}
