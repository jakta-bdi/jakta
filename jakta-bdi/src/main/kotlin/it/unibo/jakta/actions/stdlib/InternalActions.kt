package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.AbstractInternalAction
import it.unibo.jakta.actions.requests.InternalRequest
import it.unibo.tuprolog.core.Substitution

object InternalActions {
    object Print : AbstractInternalAction("print", 2) {
        override suspend fun action(request: InternalRequest) {
            val payload = request.arguments.joinToString(" ") {
                when {
                    it.isAtom -> it.castToAtom().value
                    else -> it.toString()
                }
            }
            println("[${request.agent.name}] $payload")
        }
    }

    object Fail : AbstractInternalAction("fail", 0) {
        override suspend fun action(request: InternalRequest) {
            result = Substitution.failed()
        }
    }

    object Stop : AbstractInternalAction("stop", 0) {
        override suspend fun action(request: InternalRequest) {
            stopAgent()
        }
    }

    object Pause : AbstractInternalAction("pause", 0) {
        override suspend fun action(request: InternalRequest) {
            pauseAgent()
        }
    }

    object Sleep : AbstractInternalAction("sleep", 1) {
        override suspend fun action(request: InternalRequest) {
            if (request.arguments[0].isInteger) {
                sleepAgent(request.arguments[0].castToInteger().value.toLong())
            }
        }
    }

    fun default() = mapOf(
        Print.signature.name to Print,
        Fail.signature.name to Fail,
        Stop.signature.name to Stop,
        Pause.signature.name to Pause,
        Sleep.signature.name to Sleep,
    )
}
