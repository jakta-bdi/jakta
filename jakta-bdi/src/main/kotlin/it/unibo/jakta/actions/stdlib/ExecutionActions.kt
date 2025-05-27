package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.AbstractAction
import it.unibo.jakta.actions.Action
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.effects.EventChange
import it.unibo.jakta.actions.effects.Pause
import it.unibo.jakta.actions.effects.Sleep
import it.unibo.jakta.actions.effects.Stop
import it.unibo.jakta.events.AchievementGoalFailure
import it.unibo.jakta.intentions.ASIntention
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

abstract class AbstractExecutionAction : AbstractAction() {
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

    abstract class WithoutSideEffects : Action.WithoutSideEffect, AbstractExecutionAction() {
        final override fun invoke(p1: ActionInvocationContext): List<SideEffect> {
            return super.invoke(p1)
        }
    }
}

class Print(
    vararg terms: Term,
) : AbstractExecutionAction.WithoutSideEffects() {

    private val termsList: List<Term> = terms.toList()

    constructor(list: List<Term>) : this(*list.toTypedArray())

    override fun applySubstitution(substitution: Substitution) =
        Print(termsList.map { it.apply(substitution) })

    override fun execute(context: ActionInvocationContext) {
        val payload = termsList.joinToString(" ") {
            when {
                it.isAtom -> it.castToAtom().value
                else -> it.toString()
            }
        }
        println("[${context.agentContext.agentName}] $payload")
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Print) return false
        return termsList.containsAll(other.termsList)
    }

    override fun hashCode(): Int = termsList.hashCode()
}

object Fail : AbstractExecutionAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = this

    override fun execute(context: ActionInvocationContext) {
        result = Substitution.failed()
    }
}

object Stop : AbstractExecutionAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = this

    override fun execute(context: ActionInvocationContext) {
        effects.add(Stop)
    }
}

object Pause : AbstractExecutionAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = this

    override fun execute(context: ActionInvocationContext) {
        effects.add(Pause)
    }
}

class Sleep(
    private val timeAmount: Term,
) : AbstractExecutionAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = Sleep(timeAmount.apply(substitution))

    override fun execute(context: ActionInvocationContext) {
        effects.add(Sleep(timeAmount.castToInteger().value.toLong()))
    }
}
