package it.unibo.jakta.agents.bdi.plans

import java.util.UUID

data class PlanID(val id: String = generateId()) {
    companion object {
        private fun generateId(): String = UUID.randomUUID().toString()
    }
}
