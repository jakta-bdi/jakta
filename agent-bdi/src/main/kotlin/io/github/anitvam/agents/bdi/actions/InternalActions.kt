package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.actions.impl.AbstractInternalAction
import it.unibo.tuprolog.core.Substitution

interface InternalActions {
    companion object {
        val PRINT: InternalAction = object : AbstractInternalAction("print", 2) {
            override fun action(request: InternalRequest) =
                println("[${request.agent.name}] ${request.arguments.joinToString(separator = " ")}")
        }

        val FAIL: InternalAction = object : AbstractInternalAction("fail", 0) {
            override fun action(request: InternalRequest) {
                result = Substitution.failed()
            }
        }

        val STOP: InternalAction = object : AbstractInternalAction("stop", 0) {
            override fun action(request: InternalRequest) {
                stopAgent()
            }
        }

        val PAUSE: InternalAction = object : AbstractInternalAction("pause", 0) {
            override fun action(request: InternalRequest) {
                pauseAgent()
            }
        }

        val SLEEP: InternalAction = object : AbstractInternalAction("sleep", 1) {
            override fun action(request: InternalRequest) {
                if (request.arguments[0].isInteger) {
                    Thread.sleep(request.arguments[0].castToInteger().value.toLong())
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
