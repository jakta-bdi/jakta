package io.github.anitvam.agents.bdi.goals.actions

interface InternalActions {
    companion object {
        val PRINT: InternalAction = object : ImperativeInternalAction("print", 1) {
            override fun InternalRequest.action() {
                println(arguments.first())
            }
        }

        fun default() = mapOf(PRINT.signature.name to PRINT)
    }
}
