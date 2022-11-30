package io.github.anitvam.agents.bdi.goals.actions

import it.unibo.tuprolog.core.Substitution

interface InternalActions {
    companion object {
        val PRINT: InternalAction = object : ImperativeInternalAction("print", 1) {
            override fun InternalRequest.action() = println(arguments.first())
        }

        val FAIL: InternalAction = object : ImperativeInternalAction("fail", 0) {
            override fun InternalRequest.action() {
                result = Substitution.failed()
            }
        }

        fun default() = mapOf(
            PRINT.signature.name to PRINT,
            FAIL.signature.name to FAIL,
        )
    }
}
