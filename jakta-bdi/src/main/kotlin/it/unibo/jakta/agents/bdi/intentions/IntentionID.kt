package it.unibo.jakta.agents.bdi.intentions

import java.util.UUID

data class IntentionID(
    val id: String = generateId(),
) {
    companion object {
        private fun generateId(): String = UUID.randomUUID().toString()
    }
}
