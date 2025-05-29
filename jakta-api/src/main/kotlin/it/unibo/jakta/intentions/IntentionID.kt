package it.unibo.jakta.intentions

import java.util.UUID

// TODO remove Java
data class IntentionID(val id: String = generateId()) {
    companion object {
        private fun generateId(): String = UUID.randomUUID().toString()
    }
}
