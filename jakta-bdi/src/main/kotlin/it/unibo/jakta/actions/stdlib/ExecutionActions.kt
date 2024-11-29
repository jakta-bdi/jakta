package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.effects.Pause
import it.unibo.jakta.actions.effects.Sleep
import it.unibo.jakta.actions.effects.Stop
import it.unibo.jakta.actions.requests.InternalRequest
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

class Print(
    vararg terms: Term
) : AbstractInternalAction("print", 2) {

    private var termsList: List<Term> = terms.toList()

    override suspend fun action(request: InternalRequest) {
        val payload = termsList.joinToString(" ") {
            when {
                it.isAtom -> it.castToAtom().value
                else -> it.toString()
            }
        }
        println("[${request.agentName}] $payload")
    }

    override fun applySubstitution(substitution: Substitution) {
        termsList = termsList.map { it.apply(substitution) }
    }
}

object Fail : AbstractInternalAction("fail", 0) {
    override suspend fun action(request: InternalRequest) {
        result = Substitution.failed()
    }

    override fun applySubstitution(substitution: Substitution) = Unit
}

object Stop : AbstractInternalAction("stop", 0) {
    override suspend fun action(request: InternalRequest) {
        addActionEffect(Stop)
    }

    override fun applySubstitution(substitution: Substitution) = Unit
}

object Pause : AbstractInternalAction("pause", 0) {
    override suspend fun action(request: InternalRequest) {
        addActionEffect(Pause)
    }

    override fun applySubstitution(substitution: Substitution) = Unit
}

class Sleep(
    private var timeAmount: Term
) : AbstractInternalAction("sleep", 1) {
    override suspend fun action(request: InternalRequest) {
        if (request.arguments[0].isInteger) {
            addActionEffect(Sleep(request.arguments[0].castToInteger().value.toLong()))
        }
    }

    override fun applySubstitution(substitution: Substitution) {
        timeAmount = timeAmount.apply(substitution)
    }
}
