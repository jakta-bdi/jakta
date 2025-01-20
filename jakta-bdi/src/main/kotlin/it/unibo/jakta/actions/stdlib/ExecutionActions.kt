package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.AbstractAction
import it.unibo.jakta.actions.effects.EventChange
import it.unibo.jakta.actions.effects.Pause
import it.unibo.jakta.actions.effects.Sleep
import it.unibo.jakta.actions.effects.Stop
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.events.AchievementGoalFailure
import it.unibo.jakta.intentions.ASIntention
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

abstract class AbstractExecutionAction(name: String, arity: Int): AbstractAction(name, arity) {
    override fun postExec(intention: ASIntention) {
        if (result.isSuccess) {
            if (intention.recordStack.isNotEmpty()) {
                intention.applySubstitution(result)
            }
        } else {
            val trigger = intention.currentPlan().trigger.value
            val failure = AchievementGoalFailure(trigger, intention)
            val failureEvent = EventChange.EventAddition(failure)
            effects.add(failureEvent) // Add Failure Event to be handled in future lifecycle steps
        }
    }
}

class Print(
    vararg terms: Term
) : AbstractExecutionAction("print", 2) {

    private var termsList: List<Term> = terms.toList()

    override suspend fun action(request: ActionRequest) {
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

object Fail : AbstractExecutionAction("fail", 0) {
    override suspend fun action(request: ActionRequest) {
        result = Substitution.failed()
    }

    override fun applySubstitution(substitution: Substitution) = Unit
}

object Stop : AbstractExecutionAction("stop", 0) {
    override suspend fun action(request: ActionRequest) {
        effects.add(Stop)
    }

    override fun applySubstitution(substitution: Substitution) = Unit
}

object Pause : AbstractExecutionAction("pause", 0) {
    override suspend fun action(request: ActionRequest) {
        effects.add(Pause)
    }

    override fun applySubstitution(substitution: Substitution) = Unit
}

class Sleep(
    private var timeAmount: Term
) : AbstractExecutionAction("sleep", 1) {
    override suspend fun action(request: ActionRequest) {
        effects.add(Sleep(timeAmount.castToInteger().value.toLong()))
    }

    override fun applySubstitution(substitution: Substitution) {
        timeAmount = timeAmount.apply(substitution)
    }
}
