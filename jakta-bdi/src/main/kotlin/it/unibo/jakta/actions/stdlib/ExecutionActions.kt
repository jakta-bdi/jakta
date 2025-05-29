package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.AbstractAction
import it.unibo.jakta.actions.Action
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.effects.Pause
import it.unibo.jakta.actions.effects.Sleep
import it.unibo.jakta.actions.effects.Stop
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

abstract class AbstractExecutionAction : AbstractAction() {
    abstract class WithoutSideEffects : Action.WithoutSideEffect, AbstractExecutionAction() {
        final override fun invoke(context: ActionInvocationContext): List<SideEffect> {
            return super.invoke(context)
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

object Stop : AbstractExecutionAction() {
    override fun applySubstitution(substitution: Substitution) = this

    override fun invoke(context: ActionInvocationContext): List<SideEffect> = listOf(Stop)
}

object Pause : AbstractExecutionAction() {
    override fun applySubstitution(substitution: Substitution) = this

    override fun invoke(context: ActionInvocationContext): List<SideEffect> = listOf(Pause)
}

class Sleep(
    private val timeAmount: Term,
) : AbstractExecutionAction() {
    override fun applySubstitution(substitution: Substitution) = Sleep(timeAmount.apply(substitution))

    override fun invoke(context: ActionInvocationContext) = listOf(Sleep(timeAmount.castToInteger().value.toLong()))
}
