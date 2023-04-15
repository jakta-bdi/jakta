package it.unibo.jakta.agents.bdi.actions

import it.unibo.jakta.agents.bdi.actions.impl.AbstractInternalAction
import it.unibo.tuprolog.core.Substitution

object InternalActions {
    object Print : AbstractInternalAction("print", 2) {
        override fun action(request: InternalRequest) =
            println("[${request.agent.name}] ${request.arguments.joinToString(separator = " ")}")
    }

    object Fail : AbstractInternalAction("fail", 0) {
        override fun action(request: InternalRequest) {
            result = Substitution.failed()
        }
    }

    object Stop : AbstractInternalAction("stop", 0) {
        override fun action(request: InternalRequest) {
            stopAgent()
        }
    }

    object Pause : AbstractInternalAction("pause", 0) {
        override fun action(request: InternalRequest) {
            pauseAgent()
        }
    }

    object Sleep : AbstractInternalAction("sleep", 1) {
        override fun action(request: InternalRequest) {
            if (request.arguments[0].isInteger) {
                Thread.sleep(request.arguments[0].castToInteger().value.toLong())
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
