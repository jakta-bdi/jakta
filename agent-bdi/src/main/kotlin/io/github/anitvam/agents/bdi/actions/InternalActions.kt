package io.github.anitvam.agents.bdi.actions

import it.unibo.tuprolog.core.Substitution

interface InternalActions {
    companion object {
        val PRINT: InternalAction = object : InternalAction("print", 2) {
            override fun InternalRequest.action() =
                println("[${agent.name}] ${arguments.joinToString(separator = " ")}")
        }

        val FAIL: InternalAction = object : InternalAction("fail", 0) {
            override fun InternalRequest.action() {
                result = Substitution.failed()
            }
        }

        val STOP: InternalAction = object : InternalAction("stop", 0) {
            override fun InternalRequest.action() {
                stopAgent()
            }
        }

        val PAUSE: InternalAction = object : InternalAction("pause", 0) {
            override fun InternalRequest.action() {
                pauseAgent()
            }
        }

        val SLEEP: InternalAction = object : InternalAction("sleep", 1) {
            override fun InternalRequest.action() {
                if (arguments[0].isInteger) {
                    Thread.sleep(arguments[0].castToInteger().value.toLong())
                }
            }
        }

        fun default() = mapOf(
            PRINT.signature.name to PRINT,
            FAIL.signature.name to FAIL,
            STOP.signature.name to STOP,
            PAUSE.signature.name to PAUSE,
            SLEEP.signature.name to SLEEP,
        )
    }
}
