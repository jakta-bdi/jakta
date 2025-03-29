package it.unibo.jakta.agents.bdi

import java.util.UUID

data class AgentID(
    val id: String = generateId(),
) {
    companion object {
        private fun generateId(): String = UUID.randomUUID().toString()
    }
}
