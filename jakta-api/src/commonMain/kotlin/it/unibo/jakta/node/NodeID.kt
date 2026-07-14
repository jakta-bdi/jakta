package it.unibo.jakta.node

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Unique identifier for a Plan.
 * @param[id] the identifier as a string.
 */
data class NodeID(val id: String = generateId()) {
    private companion object {
        @OptIn(ExperimentalUuidApi::class)
        private fun generateId(): String = Uuid.random().toString()
    }
}
