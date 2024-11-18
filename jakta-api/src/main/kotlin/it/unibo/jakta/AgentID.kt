package it.unibo.jakta

import java.util.*

data class AgentID(val id: String = generateId()) {
    companion object {
        private fun generateId(): String = UUID.randomUUID().toString()
    }
}
