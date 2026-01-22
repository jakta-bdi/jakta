package it.unibo.jakta.intention

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Unique identifier for an Intention.
 * @param[id] the identifier as a string.
 */
data class IntentionID(val id: String = generateId()) {

    private companion object {
        @OptIn(ExperimentalUuidApi::class)
        private fun generateId(): String = Uuid.random().toString()
    }
}
