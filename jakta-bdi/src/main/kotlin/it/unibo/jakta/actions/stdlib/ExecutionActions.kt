package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.AbstractAction
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.SideEffect
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
            if (!intention.recordStack.isEmpty()) {
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

    override fun applySubstitution(substitution: Substitution) {
        termsList = termsList.map { it.apply(substitution) }
    }

    override suspend fun invoke(context: ActionInvocationContext): List<SideEffect> {
        val payload = termsList.joinToString(" ") {
            when {
                it.isAtom -> it.castToAtom().value
                else -> it.toString()
            }
        }
        println("[${context.agent.name}] $payload")
        return emptyList()
    }
}

object Fail : AbstractExecutionAction("fail", 0) {
    override fun applySubstitution(substitution: Substitution) = Unit

    override suspend fun invoke(context: ActionInvocationContext): List<SideEffect> {
        result = Substitution.failed()
        return emptyList()
    }
}

object Stop : AbstractExecutionAction("stop", 0) {
    override fun applySubstitution(substitution: Substitution) = Unit

    override suspend fun invoke(context: ActionInvocationContext): List<SideEffect> {
        effects.add(Stop)
        return emptyList()
    }
}

object Pause : AbstractExecutionAction("pause", 0) {
    override fun applySubstitution(substitution: Substitution) = Unit

    override suspend fun invoke(context: ActionInvocationContext): List<SideEffect> {
        effects.add(Pause)
        return emptyList()
    }
}

class Sleep(
    private var timeAmount: Term
) : AbstractExecutionAction("sleep", 1) {
    override fun applySubstitution(substitution: Substitution) {
        timeAmount = timeAmount.apply(substitution)
    }

    override suspend fun invoke(context: ActionInvocationContext): List<SideEffect> {
        effects.add(Sleep(timeAmount.castToInteger().value.toLong()))
        return emptyList()
    }
}
