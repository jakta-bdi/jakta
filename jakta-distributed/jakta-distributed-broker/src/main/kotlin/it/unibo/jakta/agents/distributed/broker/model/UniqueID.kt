package it.unibo.jakta.agents.distributed.broker.model

import kotlinx.serialization.Serializable
import java.util.concurrent.atomic.AtomicInteger

@Serializable
class UniqueID(val id: String = "Mas${UniqueIDGenerator.lastId.getAndIncrement()}")

internal object UniqueIDGenerator {
    val lastId = AtomicInteger(0)
}
