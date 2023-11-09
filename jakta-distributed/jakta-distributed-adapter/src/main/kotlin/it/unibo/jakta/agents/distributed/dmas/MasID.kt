package it.unibo.jakta.agents.distributed.dmas

data class MasID(val id: String = generateId()) {
    companion object {
        private fun generateId(): String = java.util.UUID.randomUUID().toString()
    }
}
