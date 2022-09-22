package io.github.anitvam.agents.bdi

import java.util.UUID

data class PlanID(val id: String = generateId()) {
    companion object {
        private fun generateId(): String = UUID.randomUUID().toString()
    }
}
