package io.github.anitvam.agents.bdi.goals.actions

import java.util.*

data class ActionID(val id: String = generateId()) {
    companion object {
        private fun generateId(): String = UUID.randomUUID().toString()
    }
}
