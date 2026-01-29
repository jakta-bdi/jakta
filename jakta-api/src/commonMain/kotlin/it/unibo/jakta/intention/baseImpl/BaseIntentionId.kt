package it.unibo.jakta.intention.baseImpl

import it.unibo.jakta.intention.IntentionID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Unique identifier for an Intention.
 * @param[id] the identifier as a string.
 */
data class BaseIntentionID(val id: String = generateId()) : IntentionID {

    override val displayId: String
        get() = "Intention-$id"

    private companion object {
        @OptIn(ExperimentalUuidApi::class)
        private fun generateId(): String = Uuid.random().toString()
    }
}
